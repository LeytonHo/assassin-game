<#assign content>

<main-container>
    <!-- TOP ROW -->
    <div id="game-selector-container">
        <div id="game-selector" title="Your Games">
            <span>${activeGame!"<i>No games found</i>"}</span>
            <i class="fas fa-caret-down"></i>
            <div id="game-selector-list" class="hidden">
                <span></span>
                <#if gameList??>
                    <#list gameList as gameInfo>
                        <a href="/game/${gameInfo.gameID}">${gameInfo.gameName}</a>
                        <#if gameInfo.isAdmin=="true"><i class="fas fa-magic"></i></#if>
                    </#list>
                </#if>
            </div>
        </div>
    </div>
    <div id="account-options">

        <span>Welcome, ${name}!</span>
        <a href="/join-game" class="mini-button green">Join Game</a>
        <form action="/create-game" method="post">
            <input type="submit" class="mini-button" name="Create" value="Create Game"/>
        </form>
        <form action="/logout" method="post">
            <input type="submit" class="mini-button" name="Logout" value="Logout"/>
        </form>
    </div>

    <!-- BOTTOM ROW -->
    <box-content>
        <h1>Live Feed</h1>
    </box-content>
    <div> <!-- Use an extra wrapping div here for formatting -->

        <!-- MESSAGE BOXES -->
        <#if joinMessage?? && joinMessage != "">
            <box-content>
                message box :)
                ${joinMessage}
            </box-content>
        </#if>
        <#if error?? && error != "">
            <box-content class="error-message">
                ${error}
            </box-content>
        </#if>
        <#if message?? && message != "">
            <box-content>
                ${message}
            </box-content>
        </#if>

        <!-- MAIN CONTENT -->
        <box-content>

            <!-- IF GAME INFORMATION IS GIVEN -->
            <#if activeGame?? && activeGame != "">
                <h1>${activeGame}</h1>
                <p>The game is on!</p>

                <!-- TARGET BOX -->
                <p>Click below to reveal your targets:</p>
                <div class="hideable-box">
                    <div class="hideable-screen">
                        Click to show!
                    </div>
                    <div id="target-content" class="hideable-content">
                        <#if targets??>
                            <#list targets as target>
                                <span>${target}</span>
                            <#else>
                                <i>You have no targets yet!</i>
                            </#list>
                        <#else>
                            <i>You have no targets yet!</i>
                        </#if>
                    </div>
                </div>

                <!-- SECRET CODE BOX -->
                <p>Click below to see your secret code:</p>
                <div class="hideable-box">
                    <div class="hideable-screen">
                        Click to show!
                    </div>
                    <div class="hideable-content">
                        <code>
                            ${killCode}
                        </code>
                    </div>
                </div>

                <!-- TEAMMATES BOX -->
                <#if teamMembers?? && teamMembers?size != 0>
                    <p>Click below to reveal your team information and teammates:</p>
                    <div class="hideable-box">
                        <div class="hideable-screen">
                            Click to show!
                        </div>
                        <div id="target-content" class="hideable-content">
                                <#list teamMembers as teamMember>
                                    <span class=
                                          <#if teamMember.isAlive != "false"> "is-alive" <#else> "is-dead" </#if>>
                                        ${teamMember.name}
                                    </span>
                                </#list>
                        </div>
                    </div>
                </#if>

            <!-- IF NO GAME INFORMATION IS PRESENT -->
            <#else>
                <h1>Welcome, ${name}!</h1>
                <p>You are not currently in any active games.</p>
                <p>Try joining one or creating one with the "Join Game" and "Create Game" buttons!</p>
            </#if>

        </box-content>
    </div>
</main-container>

<script src="/js/home.js"></script>

</#assign>
<#include "base.ftl">