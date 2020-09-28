<#assign content>
    <div id="account-options">
        <a href="/home" class="mini-button green">Home</a>
        <form action="/logout" method="post">
            <input type="submit" class="mini-button" name="Logout" value="Logout"/>
        </form>
    </div>
    <main-content class="one-col">
        <div class="flex-column">
            <h1>Join a new game!</h1>
            <div id="error-message" class="error-message hidden"></div>
            <div id="game-code-form">
                <form class="form basic-form" id="join-game-form" onsubmit=checkJoinCode()>
                    <label for="game-code">Game Code: </label>
                    <span>
                        <input type="text" name="game-code" id="game-code" placeholder="Game Code" required>
                        <input type="submit" value="Join Game">
                    </span>
                </form>
            </div>
            <div class="two-col">
                <div class="invisible" id ="leftbox">
                    <form method="POST" class="form basic-form" id="create-team-form" action="/create-team">
                        Team Name: <input type="text" name="teamname" id="teamname" placeholder="Team Name" required>
                        <input class="hidden" name ="game-code-create" id="game-code-create">
                        <input type="submit" value="Create Team">
                    </form>
                </div>
                <div class="invisible" id ="rightbox">
                    <form method="POST" class="form basic-form" id = "join-team-form" action="/join-team">
                        Join Code: <input type="text" name="joincode" id="joincode" placeholder="Join Code" required>
                        <input class="hidden" name ="game-code-join" id="game-code-join">
                        <input type="submit" value="Join Team">
                    </form>
                </div>
            </div>
        </div>
    </main-content>

    <script src="js/join.js"></script>
</#assign>
<#include "base.ftl">