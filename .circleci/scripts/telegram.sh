#!/usr/bin/env bash
# Notificaciones CircleCI -> Telegram. Sin tokens escritos en GitHub.
set -euo pipefail

estado="${1:-}"
case "$estado" in
  EXITOSA|FALLIDA) ;;
  *) echo "Estado de notificacion no valido." >&2; exit 2 ;;
esac

# Configurar ambos valores en CircleCI > Project Settings > Environment Variables.
: "${TELEGRAM_BOT_TOKEN:?Falta TELEGRAM_BOT_TOKEN en CircleCI}"
: "${TELEGRAM_CHAT_ID:?Falta TELEGRAM_CHAT_ID en CircleCI}"

mensaje="HelpDesk UDLA - CircleCI
Estado: ${estado}
Job: ${CIRCLE_JOB:-desconocido}
Rama: ${CIRCLE_BRANCH:-desconocida}
Build: ${CIRCLE_BUILD_NUM:-desconocido}
Enlace: ${CIRCLE_BUILD_URL:-no disponible}"

# El token se utiliza solo en tiempo de ejecucion. No se imprime en consola.
curl --fail --silent --show-error --connect-timeout 10 --max-time 30 \
  --request POST \
  --data-urlencode "chat_id=${TELEGRAM_CHAT_ID}" \
  --data-urlencode "text=${mensaje}" \
  "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage" >/dev/null

echo "Notificacion Telegram enviada: ${estado}."
