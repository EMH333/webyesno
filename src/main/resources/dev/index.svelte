<script lang="ts">
  import "@shoelace-style/shoelace/dist/themes/base.css";
  import { setBasePath } from '@shoelace-style/shoelace/dist/utilities/base-path.js';
  import "@shoelace-style/shoelace/dist/components/button/button.js";
  import "@shoelace-style/shoelace/dist/components/progress-bar/progress-bar.js";
  import "@shoelace-style/shoelace/dist/components/alert/alert.js";
  import "@shoelace-style/shoelace/dist/components/icon/icon.js";
  import wretch from "wretch";
  import cookies from "cookiesjs";
  import { onMount } from "svelte";

  let showingResults = false;
  let resultPercent = 0;
  let resultVoteCount = 0;
  let resultAnswer = "";
  let loading = false;

  let errorToast: any;

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
        })
        .catch(function (error) {
          // handle error
          console.log(error);
          errorToast.toast();
        })
        .then(function () {
          // always executed
          loading = false;
        });
    };
  }

  onMount(() => {
    setBasePath("/shoelace");
    if (cookies("submitted") === true) {
      results();
    }
  });
</script>

  <sl-alert type="danger" duration="5000" closable bind:this={errorToast}>
    <sl-icon slot="icon" name="exclamation-octagon" />
    <strong>There was an error</strong><br />
    There seems to be an issue, try again later
  </sl-alert>

<div class="overall">
  {#if showingResults}
    <div class="results">
      <h1>{resultAnswer}</h1>
      <sl-progress-bar
        percentage={resultPercent}
        style="--height: 3rem; --indicator-color: var(--sl-color-{resultAnswer ==
        'YES'
          ? 'success'
          : 'danger'}-500)">{resultVoteCount} votes</sl-progress-bar
      >
    </div>
  {:else}
    <sl-button on:click={vote("yes")} disabled={loading}>Yes</sl-button>
    <br />
    <sl-button on:click={vote("no")} disabled={loading}>No</sl-button>
    <br />
    <sl-button on:click={results} {loading}>See Results</sl-button>
    <!--<button on:click={errorToast.toast()}>test</button>-->
  {/if}
</div>

<!-- markup (zero or more items) goes here -->
<style>
  /* your styles go here */
  .overall {
    margin: auto;
    text-align: center;
    display: block;
  }
  sl-button {
    margin-top: 0.5em;
  }
  .results {
    max-width: 80rem;
    margin: auto;
  }
</style>
