document.getElementById("newCheck").checked = false

newField = false

var toggleHide = function () {
    if (newField == true) {
        document.getElementById("newField").style.display = "block";
        document.getElementById("selectField").style.display = "none";
    } else {
        document.getElementById("newFieldText").value = ""
        document.getElementById("newField").style.display = "none";
        document.getElementById("selectField").style.display = "block";
    }
    newField = !newField
}

document
    .getElementById("newCheck")
    .addEventListener("click", toggleHide);

toggleHide()
