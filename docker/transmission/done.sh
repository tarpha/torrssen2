#!/bin/sh
FILE_NAME=${TR_TORRENT_NAME}

# DELETE DIRECTORY FILE TO ONLY FILE
# if [ -d "${TR_TORRENT_DIR}/${TR_TORRENT_NAME}" ]
# then
#   if [ -f "${TR_TORRENT_DIR}/${TR_TORRENT_NAME}"/*.mp4 ]
#   then
#   	FILE_NAME=$(ls "${TR_TORRENT_DIR}/${TR_TORRENT_NAME}"/*.mp4 | awk '{print $1}' | head -1 | xargs -n 1 basename)
#     mv "${TR_TORRENT_DIR}/${TR_TORRENT_NAME}"/*.mp4 "${TR_TORRENT_DIR}"
#     rm -rf "${TR_TORRENT_DIR}/${TR_TORRENT_NAME}"
#   elif [ -f "${TR_TORRENT_DIR}/${TR_TORRENT_NAME}"/*.mkv ]
#   then
#   	FILE_NAME=$(ls "${TR_TORRENT_DIR}/${TR_TORRENT_NAME}"/*.mp4 | awk '{print $1}' | head -1 | xargs -n 1 basename)
#     mv "${TR_TORRENT_DIR}/${TR_TORRENT_NAME}"/*.mkv "${TR_TORRENT_DIR}"
#     rm -rf "${TR_TORRENT_DIR}/${TR_TORRENT_NAME}"
#   fi
# fi

#CALL SERVICE
generate_post_data()
{
  cat <<EOF
{
  "id": $TR_TORRENT_ID,
  "fileName": "$FILE_NAME",
  "downloadPath": "$TR_TORRENT_DIR"
}
EOF
}

curl -d "$(generate_post_data)" -H 'Content-Type: application/json' \
http://10.0.1.10:9090/api/transmission/download-done