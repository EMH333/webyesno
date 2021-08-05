<script lang="ts">
  import "@shoelace-style/shoelace/dist/themes/base.css";
  import "@shoelace-style/shoelace/dist/components/card/card.js";
  import "@shoelace-style/shoelace/dist/components/button/button.js";
  import "@shoelace-style/shoelace/dist/components/input/input.js";
  import "@shoelace-style/shoelace/dist/components/form/form.js";
  import { setBasePath } from "@shoelace-style/shoelace/dist/utilities/base-path.js";
  import { onMount } from "svelte";
  import wretch from "wretch";

  function submitLogin(event: { detail: { formData: any } }) {
    const formData: FormData = event.detail.formData;
    let bodyData: string = "temp=test";
    formData.forEach((value, key) => {
      bodyData += `&${key.toString()}=${encodeURIComponent(value.toString())}`;
    });
    wretch("/login")
      .body(bodyData)
      .content("application/x-www-form-urlencoded")
      .post()
      .text((resp) => {
        //if resp is ok then go to index page else stay on login page
        if (resp.startsWith("OK")) {
          window.location.href = "/index.html";
        }
      })
      .catch((err) => {}); //TODO: Go into more detail here
  }

  onMount(() => {
    setBasePath("/shoelace");
  });
</script>

<sl-card class="login-form">
  <sl-form class="input-validation-type" on:sl-submit={submitLogin}>
    <sl-input
      name="email"
      type="email"
      label="Email"
      placeholder="you@example.com"
      required
    />
    <br />
    <sl-input
      name="password"
      type="password"
      label="Password"
      required
      toggle-password
    />
    <br />
    <sl-button type="primary" submit>Login</sl-button>
  </sl-form>
</sl-card>

<style>
  .login-form {
    align-items: center;
    display: flex;
    justify-content: center;
  }
</style>
