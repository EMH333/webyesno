import wretch from "wretch";

//check if session cookie exists
export function hasIDCookie(): boolean {
    return document.cookie.indexOf("ID") > -1;
}

//remove session cookie
export function removeIDCookie(): void {
    document.cookie = "ID=; expires=Thu, 01 Jan 1970 00:00:00 GMT;";
}   


//logout from server
export function logoutFromServer(): void {
    wretch("/logout")
    .post()
    .text(function (data) {
      // don't care about response at this point
      console.log(data);
    })
    .catch(function (error) {
      // handle error
      console.error(error);
    });
}
