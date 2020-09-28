// Store selectors globally
// (THESE ARE DEFINED IN HOME.JS AND SHOULD BE SAFE TO USE)
// let $gameMenu = $("#game-selector-list");
// let $errorBox = $("#error-box");
// let $messageBox = $("#message-box");

// Sets the onClick function of the game selection box and hideable boxes
$(document).ready(() => {

    const $nameform = $("#change-name");
    if ($nameform.length !== 0) {
        $nameform.click(function (_) {
            updateName();
        });
    }

    const $rulesEditToggle = $("#rules-edit-toggle");
    if ($rulesEditToggle.length !== 0) {
        $rulesEditToggle.click(function (_) {
            $rulesEditToggle.addClass("hidden");
            editRules();
        });
    }

    const $editRulesButton = $("#rules-edit-button");
    if ($editRulesButton.length !== 0) {
        $editRulesButton.click(function (_) {
            updateRules();
        });
    }

    const cancelEditRulesButton = $("#rules-edit-cancel-button");
    if (cancelEditRulesButton.length !== 0) {
        cancelEditRulesButton.click(function (_) {
            showRules();
        });
    }

    const $ruleform = $("#change-rules");
    if ($ruleform.length !== 0) {
        $ruleform.click(function (_) {
            updateRules();
        });
    }

    const $targetform = $("#change-targets");
    if ($targetform.length !== 0) {
        $targetform.click(function (_) {
            updateTargets();
        });
    }

    const $anonform = $("#change-anon");
    if ($anonform.length !== 0) {
        $anonform.click(function (_) {
            updateAnon();
        });
    }

    const $messageForm = $("#send-message");
    if ($messageForm.length !== 0) {
        $messageForm.click(function (_) {
            if ($("#send-email-option").is(":checked")) {
                sendEmail();
            } else {
                sendMessage();
            }
        });
    }

    const $startGame = $("#start-game-button");
    if ($startGame.length !== 0) {
        $startGame.click(function (_) {
            checkAndStartGame();
        })
    }
});

// Sets the onclick action of the Start Game button
function checkAndStartGame() {
    const postParameters = {
        gameid: getGameID()
    };

    // Query and put results
    // TODO: design choice: confirm box or no?
    $.post("/check-start-game", postParameters, response => {
        const jsonRes = JSON.parse(response);
        if (jsonRes.success) {
            setMessage(jsonRes.message);
        } else {
            setError(jsonRes.message);
        }
    });
}

function updateName() {
    const newName = $("#name").val();

    // Build request params
    const postParameters = {
        gameid: getGameID(),
        body: newName,
        action: "name"
    };

    $.post("/change-name", postParameters, response => {
        const jsonRes = JSON.parse(response);
        if (jsonRes.success) {
            setMessage(jsonRes.message);
        } else {
            setError(jsonRes.message);
        }
    });
}

function editRules() {
    if  ($("#rules-content").hasClass("hidden")) {
        toggleGameRules();
    }
    $("#rules-text").addClass("hidden");
    $("#rules-text-edit").removeClass("hidden");
    $("#rules-text-textarea").focus();
}

function showRules() {
    if  ($("#rules-content").hasClass("hidden")) {
        toggleGameRules();
    }
    $("#rules-edit-toggle").removeClass("hidden");
    $("#rules-text").removeClass("hidden");
    $("#rules-text-edit").addClass("hidden");
}

function updateRules() {
    const newRules = $("#rules-text-textarea").val();
    console.log(newRules);

    // Build request params
    const postParameters = {
        gameid: getGameID(),
        body: newRules,
        action: "rules"
    };

    $.post("/change-rules", postParameters, response => {
        const jsonRes = JSON.parse(response);
        if (jsonRes.success) {
            setMessage(jsonRes.message);
            showRules();
        } else {
            setError(jsonRes.message);
        }
    });
}

function updateTargets() {
    const newTargets = $("#targets").val();

    // Build request params
    const postParameters = {
        gameid: getGameID(),
        body: newTargets,
        action: "targets"
    };

    if (confirm("Change the number of targets? (This action will reassign all player targets.)")) {
        $.post("/change-targets", postParameters, response => {
            const jsonRes = JSON.parse(response);
            if (jsonRes.success) {
                setMessage(jsonRes.message);
            } else {
                setError(jsonRes.message);
            }
        });
    }
}

function updateAnon() {
    let ele = document.getElementsByName('anon');
    const anonVal = ele[0].checked;

    // Build request params
    const postParameters = {
        gameid: getGameID(),
        body: anonVal,
        action: "anon"
    };

    $.post("/change-anon", postParameters, response => {
        const jsonRes = JSON.parse(response);
        if (jsonRes.success) {
            setMessage(jsonRes.message);
        } else {
            setError(jsonRes.message);
        }
    });
}

function toggleSettings() {
    const settings = document.getElementById("settings-box");
    const button = document.getElementById("toggle-settings");
    if (settings.style.display !== "none") {
        button.innerHTML = "Show Settings";
        settings.style.display = "none";
    } else {
        button.innerHTML = "Hide Settings";
        settings.style.display = "block";
    }
}

function toggleEmail() {
    let settings = document.getElementById("email-box");
    let button = document.getElementById("toggle-email");
    if (settings.style.display !== "none") {
        button.innerHTML = "Show Message/Email Sender";
        settings.style.display = "none";
    } else {
        button.innerHTML = "Hide Message/Email Sender";
        settings.style.display = "block";
    }
}

function endGame(){

    const postParameters = {
        gameid: getGameID(),
        action: "end"
    };

    if (confirm("End this game with no winner? (This action is not reversible.)")){
        $.post("/end-game", postParameters, response => {
            const jsonRes = JSON.parse(response);

            // If success, clear error message and show the team join form
            if (jsonRes.success) {
                setMessage(jsonRes.message);
            } else {
                setError(jsonRes.message);
            }
        });
    }
}

function revive(){

    const postParameters = {
        gameid: getGameID(),
        action: "revive"
    };

    if (confirm("Revive all players on living teams? (This action is not reversible.)")) {
        $.post("/revive", postParameters, response => {
            const jsonRes = JSON.parse(response);

            if (jsonRes.success) {
                setMessage(jsonRes.message);
            } else {
                setError(jsonRes.message);
            }
        });
    }
}

function shuffle(){

    const postParameters = {
        gameid: getGameID(),
        action: "shuffle"
    };

    if (confirm("Reshuffle all targets? (This action is not reversible.)")) {
        $.post("/shuffle", postParameters, response => {
            const jsonRes = JSON.parse(response);

            if (jsonRes.success) {
                setMessage(jsonRes.message);
            } else {
                setError(jsonRes.message);
            }
        });
    }
}

function sendEmail(){
    const eSubject = $("#subject").val();
    const eBody = $("#body").val();

    // Build request params
    const postParameters = {
        gameid: getGameID(),
        subject: eSubject,
        body: eBody,
        email: "true"
    };

    if (confirm("Are you sure you want to email everyone in this game?")){
        $.post("/send-email", postParameters, response => {
            const jsonRes = JSON.parse(response);

            if (jsonRes.success) {
                setMessage(jsonRes.message);
            } else {
                setError(jsonRes.message);
            }
        });
    }
}

function sendMessage(){
    const eSubject = $("#subject").val();
    const eBody = $("#body").val();

    // Build request params
    const postParameters = {
        gameid: getGameID(),
        subject: eSubject,
        body: eBody,
        email: "false"
    };

    $.post("/send-email", postParameters, response => {
        const jsonRes = JSON.parse(response);

        if (jsonRes.success) {
            setMessage(jsonRes.message);
        } else {
            setError(jsonRes.message);
        }
    });
}