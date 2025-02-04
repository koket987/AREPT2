// Función para manejar el formulario GET
document.getElementById("getForm").addEventListener("submit", function (event) {
    event.preventDefault();
    const name = document.getElementById("name").value;
    fetch(`/app/hello?name=${name}`)
        .then(response => response.text())
        .then(data => document.getElementById("getResponse").innerText = data);
});

// Función para manejar el formulario POST
document.getElementById("postForm").addEventListener("submit", function (event) {
    event.preventDefault();
    const name = document.getElementById("postname").value;
    fetch(`/app/hello`, {
        method: "POST",
        body: `name=${name}`,
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        }
    })
        .then(response => response.text())
        .then(data => document.getElementById("postResponse").innerText = data);
});