<#assign content>

    <div class="logo">
        <a href="/"><img src="/images/ninja.png"></a>
        <span>assassin</span>
    </div>

    <main-content class="one-col">
        <div class="flex-column">
            <h1>Create a New Account!</h1>
            <div class="form basic-form" id="accountForm">
                <form method="POST" class="form basic-form" action="/create-account">
                    <input type="text" name="username" id="username" placeholder="username" required>
                    <br/>
                    <input type="text" name="email" id="email" placeholder="email" required>
                    <br/>
                    <input type="password" name="password" id="password" placeholder="password" required>
                    <br/>
                    <input type="password" name="verify" id="verify" placeholder="verify password" required>
                    <br/>
                    <input type="submit" value="Create Account">
                </form>
                <a class="small-button" href="/login">Log Into Existing Account</a>
                ${creationMessage}
            </div>
        </div>
    </main-content>

</#assign>
<#include "base.ftl">