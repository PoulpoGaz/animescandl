(function(send) {

    XMLHttpRequest.prototype.send = function(data) {
        function transferComplete(evt) {
            console.log(this.responseText);

            if (this.responseURL.startsWith("https://cdn.statically.io/img/c.japscan.ws/")) {
                console.log("%s" + this.responseText);
            }
        }

        this.addEventListener("load", transferComplete, capture=true);

        send.call(this, data);
    };

})(XMLHttpRequest.prototype.send);