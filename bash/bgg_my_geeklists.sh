#!/usr/bin/env bash

for cmd in curl perl; do
  command -v "$cmd" >/dev/null 2>&1 || { echo >&2 "I require $cmd but it's not installed.  Aborting."; exit 1; }
done

function join_by { local IFS="$1"; shift; echo "$*"; }

username=''
encpassword=''
sessionid=''

usage() { echo 1>&2 "Usage: $0 -u <username> -s <sessionid> | -p <encrypted password>"; }

while getopts "h:u:s:p:" o; do
    case "${o}" in
        h)  usage; exit 0 ;;
        u)  username="${OPTARG}" ;;
        s)  sessionid="${OPTARG}" ;;
        p)  encpassword="${OPTARG}" ;;
        *)  echo 1>&2 "Unsupported option '${o}'"; usage; exit 1 ;;
    esac
done
shift $((OPTIND-1))

[ -z "${username}" ] && { echo 1>&2 "Missing -u option"; usage; exit 1 ; }

[ -z "$sessionid" -a -z "$encpassword" ] && { echo 1>&2 "Missing -s or -p option"; usage; exit 1 ; }

cookies=()
cookies+=("bggusername=$username")
[ -z "$sessionid" ] || cookies+=("SessionID=$sessionid")
[ -z "$encpassword" ] || cookies+=("bggpassword=$encpassword")

options=()
options+=(-s)
options+=(-H "cookie: $(join_by '; ' "${cookies[@]}")")

url="https://www.boardgamegeek.com/geeklist/lists/user/$username"

curl "${options[@]}" "$url" \
	| perl -pe 's{^.*<a[^>]+href="/geeklist/(\d+)/([^"]*)"[^>]*>([^<]*)</a>.*$}{id=\1 fid=\2 name=[\3]} or $_=""'
