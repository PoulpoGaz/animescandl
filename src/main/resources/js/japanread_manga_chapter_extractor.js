(function(send) {

    XMLHttpRequest.prototype.send = function(data) {
        function transferComplete(evt) {
            console.log(this.responseText);

            var mangaRegex = /https:\/\/www\.japanread\.cc\/api\/\?id=\d+&type=manga/;
            var chapterRegex = /https:\/\/www\.japanread\.cc\/api\/\?id=\d+&type=chapter/;

            if (this.responseURL.match(mangaRegex)) {
                console.log("%s" + this.responseText);
            } else if (this.responseURL.match(chapterRegex)) {
                console.log("%s" + this.responseText);
            }
        }

        this.addEventListener("load", transferComplete, capture=true);

        send.call(this, data);
    };

})(XMLHttpRequest.prototype.send);