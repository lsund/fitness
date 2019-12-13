document.getElementById("newCheck").checked = false

newField = false

var toggleHide = function () {
    if (newField == true) {
        document.getElementById("newField").style.display = "block";
        document.getElementById("selectField").style.display = "none";
        document.getElementById("sets").value = ""
        document.getElementById("reps").value = ""
        document.getElementById("weight").value = ""
        document.getElementById("duration").value = ""
        document.getElementById("distance").value = ""
        document.getElementById("highpulse").value = ""
        document.getElementById("lowpulse").value = ""
        document.getElementById("level").value = ""
    } else {
        document.getElementById("newFieldText").value = ""
        document.getElementById("newField").style.display = "none";
        document.getElementById("selectField").style.display = "block";
    }
    newField = !newField
}

var some = function (x) {
    return x !== null && x !== "";
}

var maybeToggleCardio = function () {
    const sets = document.getElementById("sets").value;
    const reps = document.getElementById("reps").value;
    const weight = document.getElementById("weight").value;
    if (some(sets) && some(reps) && some(weight)) {
        document.getElementById("duration").style.display = "none";
        document.getElementById("distance").style.display = "none";
        document.getElementById("lowpulse").style.display = "none";
        document.getElementById("highpulse").style.display = "none";
        document.getElementById("level").style.display = "none";
    } else {
        document.getElementById("duration").style.display = "block";
        document.getElementById("distance").style.display = "block";
        document.getElementById("lowpulse").style.display = "block";
        document.getElementById("highpulse").style.display = "block";
        document.getElementById("level").style.display = "block";
    }
}

document
    .getElementById("newCheck")
    .addEventListener("click", toggleHide);

document.getElementById("sets").addEventListener("keyup", maybeToggleCardio);
document.getElementById("reps").addEventListener("keyup", maybeToggleCardio);
document.getElementById("weight").addEventListener("keyup", maybeToggleCardio);

toggleHide()
maybeToggleCardio()
