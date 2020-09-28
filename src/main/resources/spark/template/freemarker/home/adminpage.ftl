<#assign scripts>
    <script src="/js/adminpage.js"></script>
</#assign>
<#assign maininfo>

    <box-content id="main-box">

        <h0>${activeGame}</h0>
        <h2>
            You are an admin for this game!
            <#if gamePlaying != true && canStart != true>This game has ended.</#if>
        </h2>

        <#if canStart?? && canStart == true>
            <p>The join code for this game is: <code class="inline">${gameJoinCode}</code></p>
            <h2>This game has not started yet. Click the button below to start:</h2>
            <button id="start-game-button" class="small-button green">Start Game</button>
        </#if>

        <p>Click below to reveal the game's players:</p>
        <div class="hideable-box">
            <div class="hideable-screen">
                Click to show!
            </div>
            <div class="hideable-content">
                <div id="team-members" >
                    <#if playerList??>
                        <#list playerList as player>
                            <code>${player}</code>
                        <#else>
                            <i>Nobody has joined this game yet!</i>
                        </#list>
                    <#else>
                        <i>Nobody has joined this game yet!</i>
                    </#if>
                </div>
            </div>
            <a class="hideable-toggle invisible">hide</a>
        </div>

<#--        <button id="toggle-settings" class="small-button green" onclick="toggleSettings()">Hide Settings</button>-->
<#--        <button id="toggle-email" class="small-button green" onclick="toggleEmail()">Hide Message/Email Sender</button>-->

    </box-content>

    <box-content id="rules-box">
        <div id="rules-bar">
            <h0>Game Rules</h0>
            <a id="rules-bar-toggle">show rules</a>
            <a id="rules-edit-toggle">edit rules</a>
        </div>
        <div id="rules-content" class="hidden">
            <#if gameDescription?? && gameDescription != "">
                <div id="rules-text">
                    ${gameDescription!""}
                </div>
            </#if>
            <div id="rules-text-edit" class="hidden">
                <textarea class="box" id="rules-text-textarea">${gameDescriptionRaw!""}</textarea>
                <br />
                <button class="small-button green" id="rules-edit-button">Save Rules</button>
                <button class="small-button" id="rules-edit-cancel-button">Cancel Editing</button>
            </div>
            <div id="rules-extra"> <!-- TODO: rename these sections -->
                ${gameRules!""}
            </div>
        </div>
    </box-content>

    <#if gamePlaying == true>
        <box-content id="game-actions">
            <h0>Game Actions</h0>
    <#--            <label>End game now with no winner: </label>-->
            <button id="end-game" class="small-button" onclick="endGame()">End Game With No Winner</button>

    <#--            <label>Revive all players on living teams: </label>-->
            <button id="revive" class="small-button" onclick="revive()">Revive All Players On Living Teams</button>

    <#--            <label>Shuffle targets for alive teams: </label>-->
            <button id="shuffle" class="small-button" onclick="shuffle()">Shuffle Targets for All Living Teams</button>

        </box-content>
    </#if>

    <box-content id="settings-box">
        <h0>Game Settings</h0>
        <br>
        <div class="form">
            
            <label> Modify game name: </label><br>
            <input class = "input-format" type="text" name ="name" id="name" placeholder = "New name" required>
            <button id="change-name" class="small-button green">Change name</button><br>

            <label> Modify number of targets: </label><br>
            <input class = "number-format" type="number" name ="targets" id="targets" min="1" max = "3" step="1" required>
            <button id="change-targets" class="small-button green">Change number of targets</button><br>

            <label>Change anonymity status:</label><br>
            <input type="radio" id="anon" name="anon">
            <label for="anon">Keep target players anonymous (only show target codenames)</label><br>
            <input type="radio" id="deanon" name="anon">
            <label for="deanon">Show target players</label><br>
            <button id="change-anon" class="small-button green">Change anonymity</button><br>
        </div>
    </box-content>

    <box-content id = "email-box">
        <h0>Send Message to Players</h0>
        <div class="form">
            <label for="subject"> Subject: </label><br>
            <input class = "input-format" type="text" name="subject" id="subject" placeholder = "Subject" required>
            <br>
            <br>
            <label for="body"> Body: </label><br>
            <textarea class="box" name="body" id="body" placeholder="Body (HTML allowed!)" required></textarea><br>

            <input type="checkbox" id="send-email-option" name="send-email">
            <label for="send-email-option">Notify players with email</label>

            <br />
            <button id="send-message" class="small-button green">Send Announcement</button>
        </div>
    </box-content>
</#assign>
<#include "template.ftl">