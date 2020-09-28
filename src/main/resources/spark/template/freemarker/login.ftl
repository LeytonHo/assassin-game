<#assign content>

    <div class="logo">
        <a href="/"><img src="/images/ninja.png"></a>
        <span>assassin</span>
    </div>

    <main-content class="one-col">
        <div class="flex-column">
            <#if loginMessage?? && loginMessage != "">
                <div class="error-message">
                    ${loginMessage}
                </div>
            </#if>
            <h1>Log In to Your Account</h1>
            <div class="form basic-form" id="login-form">
                <form method="POST" class="form basic-form" action="/login">
                    <input type="text" name="username" id="username" placeholder="email" required>
                    <br/>
                    <input type="password" name="password" id="password" placeholder="password" required>
                    <br/>
                    <input class="btn default" type="submit" value="Log In">
                </form>
                <div class="account">
                    <form method="GET" action="/create-account">
                        <input class="btn default" type="submit" value="Create Account Instead">
                    </form>
                </div>
                <a class="small-button" href="/reset-password">Reset Password</a>
            </div>
        </div>
    </main-content>

</#assign>
<#include "base.ftl">