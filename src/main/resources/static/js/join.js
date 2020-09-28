$("#join-game-form").submit(function(event) {
    checkJoinCode(event);
});


// Handler for checking join code validity
function checkJoinCode(event) {

    event.preventDefault();
    // Find error box
    const $errorBox = $("#error-message");

    // Grab join code from input elt
    const gameCode = $("#game-code").val();

    // Build request params
    const postParameters = {
        gameJoinCode: gameCode,
    };

    // TODO: add question about checking if this is a valid game ....
    // Check if this is a valid game, and if a team needs to be created
    $.post("/check-game-code", postParameters, response => {
        const jsonRes = JSON.parse(response);

        // If success, clear error message and show the team join form
        if (jsonRes.success) {
            $errorBox.addClass("hidden");
            $("#rightbox").removeClass("invisible");
            $("#leftbox").removeClass("invisible");
            document.getElementById('game-code-create').setAttribute('value', gameCode);
            document.getElementById('game-code-join').setAttribute('value', gameCode);
        }

        else {
            $errorBox.removeClass("hidden");
            $errorBox.text(jsonRes.message);
        }
    });

}
