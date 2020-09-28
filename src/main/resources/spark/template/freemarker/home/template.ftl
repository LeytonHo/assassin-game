<#assign content>

    <!-- MESSAGE BOXES -->
    <#if joinMessage?? && joinMessage != "">
        <box-content class="message">
            ${joinMessage}
        </box-content>
    </#if>

    <box-content id="error-box-container" class="fade-in error-message <#if !error?? || error == "">hidden</#if>">
        <div id="error-box">${error!""}</div>
        <i id="error-close-button" class="fas fa-times"></i>
    </box-content>

    <box-content id="message-box-container" class="fade-in message <#if !message?? || message == "">hidden</#if>">
        <div id="message-box">${message!""}</div>
        <i id="message-close-button" class="fas fa-times"></i>
    </box-content>

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

                            <a href="/game/${gameInfo.gameID}" class="${gameInfo.status?lower_case!""}">
                                ${gameInfo.gameName}
                                <#if gameInfo.isAdmin=="true">
                                    <i class="fas fa-shield-alt"></i>
                                </#if>
                            </a>

                        </#list>
                    </#if>
                </div>
            </div>
        </div>
        <div id="account-options">

            <span>Welcome, ${name}!</span>
            <a href="/game-info" class="mini-button">Game Help</a>
            <a href="/join-game" class="mini-button green">Join Game</a>
            <form action="/create-game" method="post">
                <input type="submit" class="mini-button green" name="Create" value="Create Game"/>
            </form>
            <form action="/logout" method="post">
                <input type="submit" class="mini-button" name="Logout" value="Logout"/>
            </form>
        </div>

        <!-- BOTTOM ROW -->
        <box-content>
            <h0>Feed</h0>
            <div id="feed">
                ${killfeed}
            </div>
        </box-content>

        <!-- MAIN CONTENT -->
        <div id="main-info-container"> <!-- Use an extra wrapping div here for formatting -->
            ${maininfo}
        </div>

    </main-container>

    <script src="/js/home.js"></script>
    <span id="game-id" style="display: none">${gameID!""}</span>

</#assign>
<#include "../base.ftl">