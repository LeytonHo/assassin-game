<#assign maininfo>

    <box-content>

        <h0>${activeGame}</h0>
        <h2>${headMessage!""}</h2>

        <!-- GAME RELATED ACTIONS, WHICH ONLY SHOULD BE SHOWN IF THE GAME IS CURRENTLY ONGOING -->
        <#if gamePlaying == true>

        <!-- KILL REGISTRATION BUTTON -->
            <#if alive==true>
                <div class="kill-form basic-form left-align">
                    <label for="kill-form-code">
                        If you've killed one of your targets, enter their secret kill code here to register a kill:
                    </label>
                    <span>
                        <input type="text" id="kill-form-code" placeholder="target's code">
                        <button id="kill-form-submit" class="small-button green">Register a Kill</button>
                    </span>
                </div>
            <#elseif teamAlive==true>
                <h2>
                    You have been killed, but your team is still alive.
                </h2>
            <#else>
                <h2>
                    Your team has been eliminated.
                </h2>
            </#if>

            <!-- SECRET CODE BOX -->
            <p>Click below to see your <b>secret kill code</b>:</p>
            <div class="hideable-box">
                <div class="hideable-screen">
                    Click to show!
                </div>
                <div class="hideable-content">
                    <code>
                        ${killCode}
                    </code>
                </div>
                <a class="hideable-toggle invisible">hide</a>
            </div>

            <!-- TARGET BOX -->
            <p>Click below to reveal your targets:</p>
            <div class="hideable-box">
                <div class="hideable-screen">
                    Click to show!
                </div>
                <div id="target-content" class="hideable-content">
                    <#if targets??>
                        <#list targets as target>
                            <code><span>${target}</span></code>
                        <#else>
                            <i>You have no targets.</i>
                        </#list>
                    <#else>
                        <i>You have no targets.</i>
                    </#if>
                </div>
                <a class="hideable-toggle invisible">hide</a>
            </div>
        </#if>

        <!-- TEAM INFO BOX -->
        <p>Click below to reveal your team's information, including teammates' statuses and join code:</p>
        <div class="hideable-box">
            <div class="hideable-screen">
                Click to show!
            </div>
            <div id="team-content" class="hideable-content">
                <span id="team-name">
                    <span class="content-label">Team name:</span>
                    <span>${teamName}</span>
                </span>
                <span id="team-code">
                    <span class="content-label">Join code for this team:</span>
                    <span><code>${teamJoin}</code></span>
                </span>
                <br />
                <br />
                <span id="team-members">
                    <span class="content-label">Members of this team:</span>
                    <#if teamMembers??>
                        <#list teamMembers as teamMember>
                            <code>${teamMember}</code>
                        <#else>
                            <i>You have no teammates.</i>
                        </#list>
                    <#else>
                        <i>You have no teammates.</i>
                    </#if>
                </span>
            </div>
            <a class="hideable-toggle invisible">hide</a>
        </div>

        <!-- SURRENDER BUTTON -->
        <#if gamePlaying == true>
            <div class="surrender-form">
                <label for="surrender-form">Mark yourself as eliminated:</label>
                <span>
                    <button id="surrender" class="small-button" onclick="surrender()">Surrender</button><br>
                </span>
            </div>
        </#if>

    </box-content>

    <box-content id="rules-box">
        <div id="rules-bar">
            <h0>Game Rules</h0>
            <a id="rules-bar-toggle">show rules</a>
        </div>
        <div id="rules-content" class="hidden">
            <#if gameDescription?? && gameDescription != "">
                <div id="rules-text">
                    ${gameDescription!""}
                </div>
            </#if>
            <div id="rules-extra"> <!-- TODO: rename these sections -->
                ${gameRules!""}
            </div>
        </div>
    </box-content>

</#assign>
<#include "template.ftl">