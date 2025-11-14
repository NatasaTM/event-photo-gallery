let currentEvents = [];
let editingEventId = null;

let currentProductTypes = [];
let currentProducts = [];
let editingProductId = null;

let currentUser = null;

let currentPriceLists = [];
let editingPriceListId = null;


let currentPrices = [];
let editingPriceId = null;
let currentPriceListIdForPrices = null;

let currentEventPriceLists = [];
let currentEventIdForPriceLists = null;





document.addEventListener("DOMContentLoaded", () => {
  setupTabs();
  setupEventForm();
  loadEvents();

    setupProductTypeForm();
    setupProductForm();
    loadProductTypes();
    loadProducts();


  setupPriceListForm();
  loadPriceLists();
  setupPriceForm();

  setupEventPriceListLinking();


    loadMe();
    setupLogout();

});

async function loadMe() {
  try {
    const res = await fetch("/api/auth/me");
    if (!res.ok) {
      console.error("Failed to load user data");
      return;
    }
    const data = await res.json();
    currentUser = data; // 👈 ovde pamtimo
    document.querySelector(".user-info").textContent = data.name;
  } catch (e) {
    console.error("Could not load user info", e);
  }
}

function setupLogout() {
  const btn = document.getElementById("btn-logout");
  if (!btn) return;

  btn.addEventListener("click", async () => {
    try {
      const res = await fetch("/logout", {
        method: "POST"
      });

      // ako je Spring uradio redirect
      if (res.redirected) {
        window.location.href = res.url;
      } else {
        // fallback – vrati na login
        window.location.href = "/login.html";
      }
    } catch (e) {
      console.error("Logout error", e);
      window.location.href = "/login.html";
    }
  });
}



function setupTabs() {
  const links = document.querySelectorAll(".nav-link");
  const sections = document.querySelectorAll(".section");

  links.forEach(link => {
    link.addEventListener("click", () => {
      const target = link.getAttribute("data-section");
      links.forEach(l => l.classList.remove("active"));
      link.classList.add("active");

      sections.forEach(sec => {
        if (sec.id === `section-${target}`) {
          sec.classList.add("active");
        } else {
          sec.classList.remove("active");
        }
      });
    });
  });
}

async function loadEvents() {
  try {
    const res = await fetch("/api/events/all");
    if (!res.ok) {
      console.error("Failed to load events", res.status);
      return;
    }
    const events = await res.json();
    currentEvents = events;
    renderEvents(events);

     populateEventSelectForPriceLists(currentEvents);
  } catch (e) {
    console.error("Error fetching events", e);
  }
}

function renderEvents(events) {
  const tbody = document.querySelector("#table-events tbody");
  const count = document.getElementById("ev-count");
  tbody.innerHTML = "";

  events.forEach(ev => {
    const tr = document.createElement("tr");

    const dateStr = ev.date ? ev.date : "";
    const placeStr = ev.place || "";

    tr.innerHTML = `
      <td>${ev.id}</td>
      <td>${ev.name}</td>
      <td>${placeStr}</td>
      <td>${dateStr}</td>
      <td>
        <button type="button" class="btn-small" data-action="edit" data-id="${ev.id}">Izmeni</button>
        <button type="button" class="btn-small btn-danger" data-action="archive" data-id="${ev.id}">Arhiviraj</button>
      </td>
    `;

    tbody.appendChild(tr);
  });

  count.textContent = events.length;

  // zakači akcije
  tbody.querySelectorAll("button[data-action]").forEach(btn => {
    const action = btn.getAttribute("data-action");
    const id = parseInt(btn.getAttribute("data-id"), 10);

    if (action === "edit") {
      btn.addEventListener("click", () => startEditEvent(id));
    } else if (action === "archive") {
      btn.addEventListener("click", () => archiveEvent(id));
    }
  });
}

function setupEventForm() {
  const form = document.getElementById("form-event");
  const submitBtn = document.getElementById("btn-event-submit");
  const cancelBtn = document.getElementById("btn-event-cancel");

  cancelBtn.addEventListener("click", () => {
    resetEventForm();
  });

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const name = document.getElementById("ev-name").value.trim();
    const place = document.getElementById("ev-place").value.trim();
    const date = document.getElementById("ev-date").value; // yyyy-MM-dd

    const primaryCurrencyCode = document.getElementById("ev-primary-currency").value;
    const secondaryCurrencyCode = document.getElementById("ev-secondary-currency").value || null;

    if (!name || !date || !primaryCurrencyCode) {
      alert("Naziv, datum i primarna valuta su obavezni.");
      return;
    }

    const body = {
      name,
      place,
      date,
      primaryCurrencyCode,
      secondaryCurrencyCode
    };

    try {
      let url = "/api/events/create";
      let method = "POST";

      if (editingEventId !== null) {
        url = `/api/events/${editingEventId}`;
        method = "PUT";
      }

      const res = await fetch(url, {
        method,
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(body)
      });

      if (!res.ok) {
        console.error("Failed to save event", res.status);
        alert("Greška pri čuvanju eventa.");
        return;
      }

      resetEventForm();
      await loadEvents();
    } catch (e) {
      console.error("Error saving event", e);
      alert("Došlo je do greške pri čuvanju eventa.");
    }
  });
}

function resetEventForm() {
  const form = document.getElementById("form-event");
  const submitBtn = document.getElementById("btn-event-submit");
  const cancelBtn = document.getElementById("btn-event-cancel");

  form.reset();
  document.getElementById("ev-primary-currency").value = "RSD";
  document.getElementById("ev-secondary-currency").value = "";

  editingEventId = null;
  submitBtn.textContent = "Kreiraj event";
  cancelBtn.style.display = "none";
}

function startEditEvent(id) {
  const ev = currentEvents.find(e => e.id === id);
  if (!ev) return;

  const submitBtn = document.getElementById("btn-event-submit");
  const cancelBtn = document.getElementById("btn-event-cancel");

  document.getElementById("ev-name").value = ev.name || "";
  document.getElementById("ev-place").value = ev.place || "";
  document.getElementById("ev-date").value = ev.date || "";

  if (ev.primaryCurrencyCode) {
    document.getElementById("ev-primary-currency").value = ev.primaryCurrencyCode;
  }
  if (ev.secondaryCurrencyCode) {
    document.getElementById("ev-secondary-currency").value = ev.secondaryCurrencyCode;
  } else {
    document.getElementById("ev-secondary-currency").value = "";
  }

  editingEventId = id;
  submitBtn.textContent = "Sačuvaj izmene";
  cancelBtn.style.display = "inline-block";
}

async function archiveEvent(id) {
  if (!confirm("Da li sigurno želiš da arhiviraš ovaj event?")) {
    return;
  }

  try {
    const res = await fetch(`/api/events/${id}`, {
      method: "DELETE"
    });

    if (!res.ok) {
      console.error("Failed to archive event", res.status);
      alert("Greška pri arhiviranju eventa.");
      return;
    }

    if (editingEventId === id) {
      resetEventForm();
    }

    await loadEvents();
  } catch (e) {
    console.error("Error archiving event", e);
    alert("Došlo je do greške pri arhiviranju eventa.");
  }
}

//product type

async function loadProductTypes() {
  try {
    const res = await fetch("/api/product-types");
    if (!res.ok) {
      console.error("Failed to load product types", res.status);
      return;
    }
    const types = await res.json();
    currentProductTypes = types;
    renderProductTypes(types);
    populateProductTypeSelect(types);
  } catch (e) {
    console.error("Error fetching product types", e);
  }
}

function renderProductTypes(types) {
  const tbody = document.querySelector("#table-product-types tbody");
  tbody.innerHTML = "";

  types.forEach(pt => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${pt.id}</td>
      <td>${pt.name}</td>
    `;
    tbody.appendChild(tr);
  });
}

function populateProductTypeSelect(types) {
  const select = document.getElementById("p-type");
  select.innerHTML = "";

  types.forEach(pt => {
    const opt = document.createElement("option");
    opt.value = pt.id;
    opt.textContent = pt.name;
    select.appendChild(opt);
  });
}

function setupProductTypeForm() {
  const form = document.getElementById("form-product-type");
  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const name = document.getElementById("pt-name").value.trim();
    if (!name) {
      alert("Naziv tipa je obavezan.");
      return;
    }

    const body = { name };

    try {
      const res = await fetch("/api/product-types", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
      });

      if (!res.ok) {
        if (res.status === 409) {
          alert("Tip sa ovim nazivom već postoji.");
        } else {
          alert("Greška pri kreiranju tipa proizvoda.");
        }
        console.error("Failed to create product type", res.status);
        return;
      }

      form.reset();
      await loadProductTypes();
    } catch (e) {
      console.error("Error creating product type", e);
      alert("Došlo je do greške pri kreiranju tipa proizvoda.");
    }
  });
}
//product

async function loadProducts() {
  try {
    const res = await fetch("/api/products");
    if (!res.ok) {
      console.error("Failed to load products", res.status);
      return;
    }
    const products = await res.json();
    currentProducts = products;
    renderProducts(products);
    populateProductsSelectForPrices(products);

  } catch (e) {
    console.error("Error fetching products", e);
  }
}

function renderProducts(products) {
  const tbody = document.querySelector("#table-products tbody");
  tbody.innerHTML = "";

  products.forEach(p => {
    const tr = document.createElement("tr");
    const desc = p.description || "";
    const typeName = p.productTypeName || "";

    tr.innerHTML = `
      <td>${p.id}</td>
      <td>${p.name}</td>
      <td>${typeName}</td>
      <td>${desc}</td>
      <td>
        <button type="button" class="btn-small" data-action="edit-product" data-id="${p.id}">Izmeni</button>
        <button type="button" class="btn-small btn-danger" data-action="archive-product" data-id="${p.id}">Arhiviraj</button>
      </td>
    `;

    tbody.appendChild(tr);
  });

  tbody.querySelectorAll("button[data-action]").forEach(btn => {
    const action = btn.getAttribute("data-action");
    const id = parseInt(btn.getAttribute("data-id"), 10);

    if (action === "edit-product") {
      btn.addEventListener("click", () => startEditProduct(id));
    } else if (action === "archive-product") {
      btn.addEventListener("click", () => archiveProduct(id));
    }
  });
}

function setupProductForm() {
  const form = document.getElementById("form-product");
  const submitBtn = document.getElementById("btn-product-submit");
  const cancelBtn = document.getElementById("btn-product-cancel");

  cancelBtn.addEventListener("click", () => {
    resetProductForm();
  });

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const name = document.getElementById("p-name").value.trim();
    const description = document.getElementById("p-desc").value.trim();
    const productTypeId = document.getElementById("p-type").value;

    if (!name || !productTypeId) {
      alert("Naziv i tip proizvoda su obavezni.");
      return;
    }

    const body = {
      name,
      description,
      productTypeId: parseInt(productTypeId, 10)
    };

    try {
      let url = "/api/products";
      let method = "POST";

      if (editingProductId !== null) {
        url = `/api/products/update/${editingProductId}`;
        method = "PUT";
      }

      const res = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
      });

      if (!res.ok) {
        alert("Greška pri čuvanju proizvoda.");
        console.error("Failed to save product", res.status);
        return;
      }

      resetProductForm();
      await loadProducts();
    } catch (e) {
      console.error("Error saving product", e);
      alert("Došlo je do greške pri čuvanju proizvoda.");
    }
  });
}

function resetProductForm() {
  const form = document.getElementById("form-product");
  const submitBtn = document.getElementById("btn-product-submit");
  const cancelBtn = document.getElementById("btn-product-cancel");

  form.reset();
  if (currentProductTypes.length > 0) {
    document.getElementById("p-type").value = currentProductTypes[0].id;
  }

  editingProductId = null;
  submitBtn.textContent = "Kreiraj proizvod";
  cancelBtn.style.display = "none";
}

function startEditProduct(id) {
  const p = currentProducts.find(prod => prod.id === id);
  if (!p) return;

  const submitBtn = document.getElementById("btn-product-submit");
  const cancelBtn = document.getElementById("btn-product-cancel");

  document.getElementById("p-name").value = p.name || "";
  document.getElementById("p-desc").value = p.description || "";
  document.getElementById("p-type").value = p.productTypeId;

  editingProductId = id;
  submitBtn.textContent = "Sačuvaj izmene";
  cancelBtn.style.display = "inline-block";
}

async function archiveProduct(id) {
  if (!confirm("Da li sigurno želiš da arhiviraš ovaj proizvod?")) {
    return;
  }

  try {
    const res = await fetch(`/api/products/archive/${id}`, {
      method: "DELETE"
    });

    if (!res.ok) {
      console.error("Failed to archive product", res.status);
      alert("Greška pri arhiviranju proizvoda.");
      return;
    }

    if (editingProductId === id) {
      resetProductForm();
    }

    await loadProducts();
  } catch (e) {
    console.error("Error archiving product", e);
    alert("Došlo je do greške pri arhiviranju proizvoda.");
  }
}

//cenovnici

async function loadPriceLists() {
  try {
    const res = await fetch("/api/price-lists");
    if (!res.ok) {
      console.error("Failed to load price lists", res.status);
      return;
    }
    const data = await res.json();
    currentPriceLists = data;
    renderPriceLists(data);
    populatePriceListSelectForPrices(data);
    refreshPriceListSelectForCurrentEvent();


  } catch (e) {
    console.error("Error fetching price lists", e);
  }
}

function renderPriceLists(priceLists) {
  const tbody = document.querySelector("#table-price-lists tbody");
  tbody.innerHTML = "";

  priceLists.forEach(pl => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${pl.id}</td>
      <td>${pl.name}</td>
      <td>${pl.currencyCode}</td>
      <td>${pl.isDefault ? "✔" : ""}</td>
      <td>
        <button type="button" class="btn-small" data-action="edit-pl" data-id="${pl.id}">Izmeni</button>
        <button type="button" class="btn-small btn-danger" data-action="delete-pl" data-id="${pl.id}">Obriši</button>
      </td>
    `;
    tbody.appendChild(tr);
  });

  tbody.querySelectorAll("button[data-action]").forEach(btn => {
    const action = btn.getAttribute("data-action");
    const id = parseInt(btn.getAttribute("data-id"), 10);

    if (action === "edit-pl") {
      btn.addEventListener("click", () => startEditPriceList(id));
    } else if (action === "delete-pl") {
      btn.addEventListener("click", () => deletePriceList(id));
    }
  });
}
function setupPriceListForm() {
  const form = document.getElementById("form-price-list");
  const submitBtn = document.getElementById("btn-pl-submit");
  const cancelBtn = document.getElementById("btn-pl-cancel");

  cancelBtn.addEventListener("click", () => {
    resetPriceListForm();
  });

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const name = document.getElementById("pl-name").value.trim();
    const currencyCode = document.getElementById("pl-currency").value;
    const isDefault = document.getElementById("pl-is-default").checked;

    if (!name || !currencyCode) {
      alert("Naziv i valuta su obavezni.");
      return;
    }

    const body = { name, currencyCode, isDefault };

    try {
      let url = "/api/price-lists";
      let method = "POST";

      if (editingPriceListId !== null) {
        url = `/api/price-lists/update/${editingPriceListId}`;
        method = "PUT";
      }

      const res = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
      });

      if (!res.ok) {
        alert("Greška pri čuvanju cenovnika.");
        console.error("Failed to save price list", res.status);
        return;
      }

      resetPriceListForm();
      await loadPriceLists();
    } catch (e) {
      console.error("Error saving price list", e);
      alert("Došlo je do greške pri čuvanju cenovnika.");
    }
  });
}

function resetPriceListForm() {
  const form = document.getElementById("form-price-list");
  const submitBtn = document.getElementById("btn-pl-submit");
  const cancelBtn = document.getElementById("btn-pl-cancel");

  form.reset();
  document.getElementById("pl-currency").value = "RSD";
  document.getElementById("pl-is-default").checked = false;

  editingPriceListId = null;
  submitBtn.textContent = "Kreiraj cenovnik";
  cancelBtn.style.display = "none";
}

function startEditPriceList(id) {
  const pl = currentPriceLists.find(p => p.id === id);
  if (!pl) return;

  const submitBtn = document.getElementById("btn-pl-submit");
  const cancelBtn = document.getElementById("btn-pl-cancel");

  document.getElementById("pl-name").value = pl.name || "";
  document.getElementById("pl-currency").value = pl.currencyCode || "RSD";
  document.getElementById("pl-is-default").checked = !!pl.isDefault;

  editingPriceListId = id;
  submitBtn.textContent = "Sačuvaj izmene";
  cancelBtn.style.display = "inline-block";
}

async function deletePriceList(id) {
  if (!confirm("Da li sigurno želiš da obrišeš ovaj cenovnik?")) {
    return;
  }

  try {
    const res = await fetch(`/api/price-lists/${id}`, {
      method: "DELETE"
    });

    if (!res.ok) {
      alert("Greška pri brisanju cenovnika.");
      console.error("Failed to delete price list", res.status);
      return;
    }

    if (editingPriceListId === id) {
      resetPriceListForm();
    }

    await loadPriceLists();
  } catch (e) {
    console.error("Error deleting price list", e);
    alert("Došlo je do greške pri brisanju cenovnika.");
  }
}

function populatePriceListSelectForPrices(priceLists) {
  const select = document.getElementById("price-pl-select");
  if (!select) return;

  select.innerHTML = "";
  priceLists.forEach(pl => {
    const opt = document.createElement("option");
    opt.value = pl.id;
    opt.textContent = `${pl.name} (${pl.currencyCode})`;
    select.appendChild(opt);
  });

  if (priceLists.length > 0) {
    currentPriceListIdForPrices = priceLists[0].id;
    select.value = currentPriceListIdForPrices;
    loadPricesForCurrentPriceList();
  } else {
    currentPriceListIdForPrices = null;
    document.querySelector("#table-prices tbody").innerHTML = "";
  }

  select.addEventListener("change", () => {
    currentPriceListIdForPrices = parseInt(select.value, 10);
    loadPricesForCurrentPriceList();
    resetPriceForm();
  });
}
function populateProductsSelectForPrices(products) {
  const select = document.getElementById("price-product");
  if (!select) return;

  select.innerHTML = "";
  products.forEach(p => {
    const opt = document.createElement("option");
    opt.value = p.id;
    opt.textContent = p.name;
    select.appendChild(opt);
  });
}
async function loadPricesForCurrentPriceList() {
  if (!currentPriceListIdForPrices) {
    currentPrices = [];
    renderPrices([]);
    return;
  }

  try {
    const res = await fetch(`/api/prices/by-price-list/${currentPriceListIdForPrices}`);
    if (!res.ok) {
      console.error("Failed to load prices", res.status);
      return;
    }
    const prices = await res.json();
    currentPrices = prices;
    renderPrices(prices);
  } catch (e) {
    console.error("Error fetching prices", e);
  }
}

function renderPrices(prices) {
  const tbody = document.querySelector("#table-prices tbody");
  tbody.innerHTML = "";

  prices.forEach(pr => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${pr.id}</td>
      <td>${pr.productName}</td>
      <td>${pr.priceAmount}</td>
      <td>${pr.taxRate}</td>
      <td>${pr.minQty}</td>
      <td>
        <button type="button" class="btn-small" data-action="edit-price" data-id="${pr.id}">Izmeni</button>
        <button type="button" class="btn-small btn-danger" data-action="delete-price" data-id="${pr.id}">Obriši</button>
      </td>
    `;
    tbody.appendChild(tr);
  });

  tbody.querySelectorAll("button[data-action]").forEach(btn => {
    const action = btn.getAttribute("data-action");
    const id = parseInt(btn.getAttribute("data-id"), 10);

    if (action === "edit-price") {
      btn.addEventListener("click", () => startEditPrice(id));
    } else if (action === "delete-price") {
      btn.addEventListener("click", () => deletePrice(id));
    }
  });
}

function setupPriceForm() {
  const form = document.getElementById("form-price");
  if (!form) return;

  const submitBtn = document.getElementById("btn-price-submit");
  const cancelBtn = document.getElementById("btn-price-cancel");

  cancelBtn.addEventListener("click", () => {
    resetPriceForm();
  });

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    if (!currentPriceListIdForPrices) {
      alert("Prvo izaberi cenovnik.");
      return;
    }

    const productId = parseInt(document.getElementById("price-product").value, 10);
    const priceAmount = parseFloat(document.getElementById("price-amount").value);
    const taxRate = parseInt(document.getElementById("price-tax").value || "0", 10);
    const minQty = parseInt(document.getElementById("price-minqty").value || "1", 10);

    if (!productId || isNaN(priceAmount)) {
      alert("Proizvod i cena su obavezni.");
      return;
    }

    const body = {
      productId,
      priceListId: currentPriceListIdForPrices,
      priceAmount,
      taxRate,
      minQty
    };

    try {
      let url = "/api/prices";
      let method = "POST";

      if (editingPriceId !== null) {
        url = `/api/prices/update/${editingPriceId}`;
        method = "PUT";
      }

      const res = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
      });

      if (!res.ok) {
        alert("Greška pri čuvanju cene.");
        console.error("Failed to save price", res.status);
        return;
      }

      resetPriceForm();
      await loadPricesForCurrentPriceList();
    } catch (e) {
      console.error("Error saving price", e);
      alert("Došlo je do greške pri čuvanju cene.");
    }
  });
}

function resetPriceForm() {
  const form = document.getElementById("form-price");
  if (!form) return;

  const submitBtn = document.getElementById("btn-price-submit");
  const cancelBtn = document.getElementById("btn-price-cancel");

  form.reset();
  editingPriceId = null;
  submitBtn.textContent = "Dodaj cenu";
  cancelBtn.style.display = "none";

  // auto-set default minQty & tax
  document.getElementById("price-tax").value = "0";
  document.getElementById("price-minqty").value = "1";
}

function startEditPrice(id) {
  const pr = currentPrices.find(p => p.id === id);
  if (!pr) return;

  const submitBtn = document.getElementById("btn-price-submit");
  const cancelBtn = document.getElementById("btn-price-cancel");

  document.getElementById("price-product").value = pr.productId;
  document.getElementById("price-amount").value = pr.priceAmount;
  document.getElementById("price-tax").value = pr.taxRate;
  document.getElementById("price-minqty").value = pr.minQty;

  editingPriceId = id;
  submitBtn.textContent = "Sačuvaj izmenu";
  cancelBtn.style.display = "inline-block";
}

async function deletePrice(id) {
  if (!confirm("Da li sigurno želiš da obrišeš ovu cenu?")) {
    return;
  }

  try {
    const res = await fetch(`/api/prices/${id}`, {
      method: "DELETE"
    });

    if (!res.ok) {
      alert("Greška pri brisanju cene.");
      console.error("Failed to delete price", res.status);
      return;
    }

    if (editingPriceId === id) {
      resetPriceForm();
    }

    await loadPricesForCurrentPriceList();
  } catch (e) {
    console.error("Error deleting price", e);
    alert("Došlo je do greške pri brisanju cene.");
  }
}

//cenovnici za events

function populateEventSelectForPriceLists(events) {
  const select = document.getElementById("evpl-event");
  if (!select) return;

  select.innerHTML = "";

  events.forEach(ev => {
    const opt = document.createElement("option");
    opt.value = ev.id;
    opt.textContent = `${ev.name} (${ev.date || ""})`;
    select.appendChild(opt);
  });

  if (events.length > 0) {
    currentEventIdForPriceLists = events[0].id;
    select.value = currentEventIdForPriceLists;
    // kad imamo i cenovnike, filtriramo
    refreshPriceListSelectForCurrentEvent();
    loadEventPriceLists(currentEventIdForPriceLists);
  } else {
    currentEventIdForPriceLists = null;
    document.querySelector("#table-evpl tbody").innerHTML = "";
  }

  select.addEventListener("change", () => {
    currentEventIdForPriceLists = parseInt(select.value, 10);
    refreshPriceListSelectForCurrentEvent();
    loadEventPriceLists(currentEventIdForPriceLists);
  });
}

function refreshPriceListSelectForCurrentEvent() {
  const select = document.getElementById("evpl-pricelist");
  if (!select) return;
  select.innerHTML = "";

  if (!currentEventIdForPriceLists || currentEvents.length === 0 || currentPriceLists.length === 0) {
    return;
  }

  const ev = currentEvents.find(e => e.id === currentEventIdForPriceLists);
  if (!ev) return;

  const allowedCurrencies = [];
  if (ev.primaryCurrencyCode) allowedCurrencies.push(ev.primaryCurrencyCode);
  if (ev.secondaryCurrencyCode) allowedCurrencies.push(ev.secondaryCurrencyCode);

  const filtered = currentPriceLists.filter(pl => allowedCurrencies.includes(pl.currencyCode));

  filtered.forEach(pl => {
    const opt = document.createElement("option");
    opt.value = pl.id;
    opt.textContent = `${pl.name} (${pl.currencyCode})`;
    select.appendChild(opt);
  });
}

async function loadEventPriceLists(eventId) {
  if (!eventId) {
    currentEventPriceLists = [];
    renderEventPriceLists([]);
    return;
  }

  try {
    const res = await fetch(`/api/events/${eventId}/price-lists`);
    if (!res.ok) {
      console.error("Failed to load event price lists", res.status);
      return;
    }
    const data = await res.json();
    currentEventPriceLists = data;
    renderEventPriceLists(data);
  } catch (e) {
    console.error("Error fetching event price lists", e);
  }
}

function renderEventPriceLists(items) {
  const tbody = document.querySelector("#table-evpl tbody");
  if (!tbody) return;
  tbody.innerHTML = "";

  items.forEach(item => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${item.id}</td>
      <td>${item.priceListName}</td>
      <td>${item.currencyCode}</td>
      <td>${item.defaultForCurrency ? "✔" : ""}</td>
      <td>
        <button type="button" class="btn-small" data-action="set-default-evpl" data-id="${item.id}">Default</button>
        <button type="button" class="btn-small btn-danger" data-action="delete-evpl" data-id="${item.id}">Ukloni</button>
      </td>
    `;
    tbody.appendChild(tr);
  });

  tbody.querySelectorAll("button[data-action]").forEach(btn => {
    const action = btn.getAttribute("data-action");
    const id = parseInt(btn.getAttribute("data-id"), 10);

    if (action === "set-default-evpl") {
      btn.addEventListener("click", () => setDefaultEventPriceList(id));
    } else if (action === "delete-evpl") {
      btn.addEventListener("click", () => deleteEventPriceList(id));
    }
  });
}

function setupEventPriceListLinking() {
  const btn = document.getElementById("btn-evpl-link");
  if (!btn) return;

  btn.addEventListener("click", async () => {
    if (!currentEventIdForPriceLists) {
      alert("Prvo izaberi event.");
      return;
    }

    const priceListSelect = document.getElementById("evpl-pricelist");
    if (!priceListSelect || !priceListSelect.value) {
      alert("Prvo izaberi cenovnik.");
      return;
    }

    const priceListId = parseInt(priceListSelect.value, 10);
    const isDefault = document.getElementById("evpl-default").checked;

    const body = {
      priceListId,
      defaultForCurrency: isDefault
    };

    try {
      const res = await fetch(`/api/events/${currentEventIdForPriceLists}/price-lists`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
      });

      if (!res.ok) {
        alert("Greška pri povezivanju cenovnika i eventa.");
        console.error("Failed to link price list", res.status);
        return;
      }

      document.getElementById("evpl-default").checked = false;
      await loadEventPriceLists(currentEventIdForPriceLists);
    } catch (e) {
      console.error("Error linking price list to event", e);
      alert("Došlo je do greške pri povezivanju cenovnika i eventa.");
    }
  });
}

async function setDefaultEventPriceList(id) {
  try {
    const res = await fetch(`/api/event-price-lists/${id}/set-default`, {
      method: "PUT"
    });

    if (!res.ok) {
      alert("Greška pri postavljanju default cenovnika.");
      console.error("Failed to set default", res.status);
      return;
    }

    await loadEventPriceLists(currentEventIdForPriceLists);
  } catch (e) {
    console.error("Error setting default event price list", e);
    alert("Došlo je do greške pri postavljanju default cenovnika.");
  }
}

async function deleteEventPriceList(id) {
  if (!confirm("Da li sigurno želiš da ukloniš ovaj cenovnik sa eventa?")) {
    return;
  }

  try {
    const res = await fetch(`/api/event-price-lists/${id}`, {
      method: "DELETE"
    });

    if (!res.ok) {
      alert("Greška pri uklanjanju cenovnika sa eventa.");
      console.error("Failed to delete event price list", res.status);
      return;
    }

    await loadEventPriceLists(currentEventIdForPriceLists);
  } catch (e) {
    console.error("Error deleting event price list", e);
    alert("Došlo je do greške pri uklanjanju cenovnika sa eventa.");
  }
}


