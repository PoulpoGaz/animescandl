# ANIMESCANDL

A program for downloading scans or anime from various websites.

## Supported websites

Scans:
* [Japanread](https://www.japanread.cc) (fr)
* [Japscan](https://www.japscan.ws) (fr)
* [Mangadex](https://mangadex.org) (all languages)
* [Sushiscan](https://sushi-scan.su) (fr)

Anime:
* [Nekosama](https://neko-sama.fr) (fr)

## Requirements

* [Opera driver](https://github.com/operasoftware/operachromiumdriver) (for japanread and japscan).
  The driver should be in a folder named "drivers" next to animescandl and should named "operadriver". 
  You can also specify the path with --opera (-op) option.
* [ffmpeg](https://ffmpeg.org/download.html) (for neko-sama).
  ffmpeg executable should be in the PATH. You can alos specify the path with --ffmepg (-ff) option.

## Usage

First create a file named "animescandl.json" next to animescandl executable.
It is a json file which looks like this:

```json
[
    {
        "name": "https://neko-sama.fr/anime/info/4973-steins-gate-vostfr",
        "range": "1,5-7"
    },
    {
        "name": "Re:zero",
        "concatenateAll": true,
        "out": "PATH"
    }
]
```

It is an array of objects containing at least a "name". All others "arguments" are optional.

### Arguments

* <strong>name</strong>: can be a direct url to a website or a just a string. 
  In the last case, the program will search on all websites a scan/anime matching 
  the search string.
* <strong>range</strong>: A range looks like "1,5-7". It specifies which scans/animes
  will be downloaded.
* <strong>concatenateAll</strong>: For scans, it will concatenate all scans and produce a big pdf
* <strong>out</strong>: A folder where you want to download.

## Developers

This program use maven.<br>
To create a jar, execute:
```bash
mvn clean compile assembly:single
```