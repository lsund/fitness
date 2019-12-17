document.getElementById("newCheck").checked = false

newField = false

var hideElements = (xs) => {
    xs.forEach(x => document.getElementById(x).style.display = "none");
};

var showElements = (xs) => {
    xs.forEach(x => document.getElementById(x).style.display = "block");
};

var removeContentsOf = (xs) => {
    xs.forEach(x => document.getElementById(x).value = "");
};

var toggleHide = function () {
    if (newField == true) {
        document.getElementById("newField").style.display = "block";
        hideElements(["selectField"]);
        const textfields = ["sets" , "reps", "weight", "duration", "distance", "highpulse", "lowpulse", "level"];
        removeContentsOf(textfields);
    } else {
        document.getElementById("newFieldText").value = ""
        hideElements(["newField"]);
        document.getElementById("selectField").style.display = "block";
    }
    newField = !newField
    maybeToggleCardio()
}

var some = function (x) {
    return x !== null && x !== "";
}

var maybeToggleCardio = function () {
    const sets = document.getElementById("sets").value;
    const reps = document.getElementById("reps").value;
    const weight = document.getElementById("weight").value;
    const cardioFields = ["duration", "distance", "lowpulse", "highpulse", "level"];
    if (some(sets) && some(reps) && some(weight)) {
        hideElements(cardioFields);
    } else {
        showElements(cardioFields);
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
