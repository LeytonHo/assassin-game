<#assign content>

    <div class="logo">
        <a href="/">
            <img src="/images/ninja.png">
        </a>
        <h1>assassin</h1>
    </div>

    <main-content class="one-col">
        <div class="flex-column">
            <h1>Reset Password</h1>
            <div class="form basic-form" id="accountForm">
                <form method="POST" class="form basic-form" action="/reset-password">
                    <input type="text" name="email" id="email" placeholder="email" required>
                    <br/>
                    <input type="password" name="current" id="current" placeholder="current password" required>
                    <br/>
                    <input type="password" name="new" id="new" placeholder="new password" required>
                    <br/>
                    <input type="password" name="verify" id="verify" placeholder="verify new password" required>
                    <br/>
                    <input type="submit" value="Reset Password">
                </form>
                <a class="small-button" href="/login">Log Into Existing Account</a>
                ${message}
            </div>
        </div>
    </main-content>

</#assign>
<#include "base.ftl">