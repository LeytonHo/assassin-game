// Store selectors globally
let $gameMenu = $("#game-selector-list");
let $errorBox = $("#error-box");
let $errorContainer = $("#error-box-container");
let $messageBox = $("#message-box");
let $messageContainer = $("#message-box-container");

// Sets the onClick function of the game selection box and hideable boxes
$(document).ready(() => {

    // Sets the click function for the game selection menu
    $gameMenu = $("#game-selector-list");
    $("#game-selector").click(function(_) {
        toggleGameSelector();
    });

    $(".hideable-box").each(function() {
        const $hildeableToggle = $(this).children(".hideable-toggle")
        const $screen = $(this).children(".hideable-screen");

        // Sets the click function for hideable boxes
        $screen.click(function(_) {
            $screen.addClass("invisible");
            $hildeableToggle.removeClass("invisible");
        })

        // Sets the hide function for hideable box link
        $hildeableToggle.click(function(_) {
            $screen.removeClass("invisible");
            $hildeableToggle.addClass("invisible");
        });
    });

    $("#rules-bar-toggle").click(function(_) {
        toggleGameRules();
    })

    // Sets the click function for the error and message close buttons
    $("#error-close-button").click(getCloseFunctionFor($errorContainer));
    $("#message-close-button").click(getCloseFunctionFor($messageContainer));

    // Sets the click function for the kill registration button
    const $killForm = $("#kill-form-submit");
    if ($killForm.length !== 0) {
        $killForm.click(function (_) {
            checkAndExecuteKill();
        });
    }
});

// Toggles the game selector menu display
function toggleGameSelector() {
    $gameMenu.toggleClass("hidden");
}

// Make the game selector menu close if clicked outside
jQuery(document).click((e) => {
    if (!jQuery(e.target).is("#game-selector-container")
        && !jQuery(e.target).is("#game-selector-container *")
        && !$gameMenu.hasClass("hidden")) {
        $gameMenu.addClass("hidden");
    }
});

// Toggles the game rules display
function toggleGameRules() {
    // Since we need to change the show/hide text, don't use toggle
    const $gameRulesToggle = $("#rules-bar-toggle");
    const $gameRulesContent = $("#rules-content");

    if ($gameRulesContent.hasClass("hidden")) {
        $gameRulesContent.removeClass("hidden");
        $gameRulesToggle.html("hide rules");
    } else {
        $gameRulesContent.addClass("hidden");
        $gameRulesToggle.html("show rules");
    }
}

function getCloseFunctionFor(elt) {
    return (function(_) {
        elt.addClass("hidden");
    });
}

function getGameID() {
    return $("#game-id").text();
}

function setMessage(message) {
    $errorContainer.addClass("hidden");

    $messageContainer.removeClass("loaded")
        .removeClass("hidden")
        .offsetHeight;
        // offsetHeight() => forces reflow (theoretically)
    $messageContainer.addClass("loaded");
    $messageBox.text(message);
}

function setError(error) {
    $messageContainer.addClass("hidden");

    $errorContainer.removeClass("loaded")
        .removeClass("hidden")
        .offsetHeight;
        // offsetHeight() => forces reflow
    $errorContainer.addClass("loaded");
    $errorBox.text(error);
}


// Set the onclick action of the kill registration button
function checkAndExecuteKill() {
    const killCode = $("#kill-form-code").val();

    // Build request params
    const postParameters = {
        gameid: getGameID(),
        killcode: killCode
    };

    // Query and put results
    $.post("/check-kill-code", postParameters, response => {
        const jsonRes = JSON.parse(response);

        // If success, clear error message and show the team join form
        if (jsonRes.success) {
            setMessage(jsonRes.message);
        } else {
            setError(jsonRes.message);
        }
    });
}

// Set the onclick action of the kill registration button
function surrender() {
    // Build request params
    const postParameters = {
        gameid: getGameID(),
    };

    // Query and put results
    if (confirm("Are you really sure you want to surrender? This can't be undone.")){
        $.post("/surrender", postParameters, response => {
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