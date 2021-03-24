<script lang="ts">
  import "@shoelace-style/shoelace/dist/themes/base.css";
  import "@shoelace-style/shoelace/dist/components/button/button.js";
  import "@shoelace-style/shoelace/dist/components/progress-bar/progress-bar.js";
  import wretch from "wretch";
  import cookies from "cookiesjs";
  import { onMount } from "svelte";

  let showingResults = false;
  let resultPercent = 0;
  let resultVoteCount = 0;
  let resultAnswer = "";
  let loading = false;

  let errorToast: any;
  let error = false;

  function results() {
    loading = true;
    wretch("/result")
      .get()
      .text(function (data) {
        // handle success
        //console.log(response);
        const resp = data.split(";");
        resultAnswer = resp[0];
        resultVoteCount = Number.parseInt(resp[1]);
        resultPercent = (resultVoteCount / Number.parseInt(resp[2])) * 100;
        showingResults = true;
      })
      .catch(function (error) {
        // handle error
        console.log(error);
        error = true;
        errorToast.toast();
      })
      .then(function () {
        // always executed
        loading = false;
      });
  }

  function vote(answer: string) {
    return function () {
      loading = true;
      wretch("/answer")
        .post(answer)
        .res((response) => {
          results(); //call results to display results after voting
        });
    };
  }

  onMount(() => {
    if (cookies("submitted") === true) {
      results();
    }
  });
</script>

{#if error}
  <sl-alert type="danger" duration="5000" closable bind:this={errorToast}>
    <sl-icon slot="icon" name="exclamation-octagon" />
    <strong>There was an error</strong><br />
    There seems to be an issue, try again later
  </sl-alert>
{/if}

{#if showingResults}
  <h1 class="results">{resultAnswer}</h1>
  <sl-progress-bar
    percentage={resultPercent}
    class="results"
    style="--height: 3rem;">{resultVoteCount}</sl-progress-bar
  >
{:else}
  <sl-button on:click={vote("yes")} disabled={loading}>Yes</sl-button>
  <sl-button on:click={vote("no")} disabled={loading}>No</sl-button>
  <sl-button on:click={results} {loading}>See Results</sl-button>
{/if}

<!-- markup (zero or more items) goes here -->
<style>
  /* your styles go here */
</style>
