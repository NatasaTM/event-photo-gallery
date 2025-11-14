let currentEvents = [];
let editingEventId = null;

document.addEventListener("DOMContentLoaded", () => {
  setupTabs();
  setupEventForm();
  loadEvents();
});

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
