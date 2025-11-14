let currentEvents = [];
let editingEventId = null;

let currentProductTypes = [];
let currentProducts = [];
let editingProductId = null;

let currentUser = null;


document.addEventListener("DOMContentLoaded", () => {
  setupTabs();
  setupEventForm();
  loadEvents();

    setupProductTypeForm();
    setupProductForm();
    loadProductTypes();
    loadProducts();

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

