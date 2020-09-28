<#assign content>
    <div id="account-options">
        <a href="/home" class="mini-button green">Home</a>
        <form action="/logout" method="post">
            <input type="submit" class="mini-button" name="Logout" value="Logout"/>
        </form>
    </div>
    <main-content class="one-col flex-column">
        <h1>Create a New Game!</h1>
        <div class="basic-form" id="accountForm">
            <form method="POST" action="/create-game">
                <label> Game Name: </label>
                <input type="text" name ="name" id="name" placeholder = "Game name" required>
                <br/>
                <label> Game Rules: </label>
                <br/>
                <textarea class="box" name="description" id="description" placeholder="Rules (Markdown supported)"></textarea>
                <br/>
                <label>Max Team Size</label>
                <input type="number" name ="size" id="size" min="1" max = "4" step="1" required>
                <br/>
                <label>Max number of Targets</label>
                <input type="number" name ="targets" id="targets" min="1" max = "3" step="1" required>
                <br/>
                <label>Prevent players from seeing target team members?</label>
                <input type="checkbox" name="anon" id="anon">
                <br/>
                <input type="submit" value="Create Game">
            </form>

        </div>
    </main-content>

</#assign>
<#include "base.ftl">