<#assign content>

    <main-content class="one-col">
        <div class="flex-column padded">
            <#if header??>
                <h1>${header}</h1>
            </#if>
            <p>
                ${message}
            </p>
            <br />
            <a class="small-button green" href="/home">Return to Homepage</a>
        </div>
    </main-content>

</#assign>
<#include "base.ftl">