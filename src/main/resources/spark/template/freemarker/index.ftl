<#assign content>

<div class="logo">
    <a href="/"><img src="/images/ninja.png"></a>
    <span>assassin</span>
</div>
<#--<div id="account-options">-->
<#--    <a href="/game-info" class="mini-button">Game Info</a>-->
<#--</div>-->

<main-content>
    <div class="flex-column">
        <div class="welcome">
            <h1> Assassin Game Online </h1>
            <p> <i>Host your live-action assassin game here! Assassin is a tradition across high schools and colleges,
                and <b>it's never been easier to organize and oversee</b>.
                    <a href="/game-info">Click here for details on using this tool!</a></i>
            </p>
        </div>
    </div>
    <div class="two-col">
        <div id="splash-column1" class="flex-column">
            <div id="feature-box">
                <div id="feature-logo">
                    <i class="fas fa-project-diagram"></i>
                </div>
                <div id="feature-desc">
                    Optimized target assignment
                </div>

                <div id="feature-logo">
                    <i class="fas fa-bullseye"></i>
                </div>
                <div id="feature-desc">
                    Simple kill registration
                </div>

                <div id="feature-logo">
                    <i class="fas fa-random"></i>
                </div>
                <div id="feature-desc">
                    Game admin options
                </div>

                <div id="feature-logo">
                    <i class="far fa-list-alt"></i>
                </div>
                <div id="feature-desc">
                    Live feed and email updates
                </div>
            </div>
            <p>
                <b>Log in or sign up to create or join a game!</b>
            </p>
        </div>
        <div id="splash-column2" class="flex-column">
            <a href="${signupURL}" class="big-button green">Sign Up</a>
            <a href="${loginURL}" class="big-button">Log In</a>
        </div>
    </div>
</main-content>

</#assign>
<#include "base.ftl">