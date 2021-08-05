<script lang="typescript">
  import "@shoelace-style/shoelace/dist/themes/base.css";
  import "@shoelace-style/shoelace/dist/components/button/button.js";
  import { onMount } from "svelte";
  import { hasIDCookie, logoutFromServer, removeIDCookie } from "./loginUtil";
  import type SlButton from "@shoelace-style/shoelace/dist/components/button/button.js";

  let button: SlButton;
  let content: string = "Login";
  let location: string = "/login.html";

  // Get current login status and update button text and location
  onMount(updateButton);

  function onClick(event: Event) {
    if (hasIDCookie()) {
      event.preventDefault(); // don't propogate if logging out
      // Logout
      logoutFromServer();
      removeIDCookie();
    } else {
      updateButton();
    }
  }

  function updateButton() {
    if (hasIDCookie()) {
      content = "Logout";
      location = "";
    } else {
      content = "Login";
      location = "/login.html";
    }
  }
</script>

<sl-button type="default" href={location} bind:this={button} on:click={onClick}
  >{content}</sl-button
>

<style>
</style>
