#!/bin/sh
# Sends docs/wiki to https://folclorebr.dantas-dev.com (nginx static site on my_vps).
# Usage: sh tools/publicar_wiki.sh
set -e
cd "$(dirname "$0")/../docs/wiki"
scp -q -r index.html img my_vps:/var/www/folclorebr/
ssh my_vps 'chown -R www-data:www-data /var/www/folclorebr'
echo "wiki publicada: https://folclorebr.dantas-dev.com"
