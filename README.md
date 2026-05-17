# ParkEase

## Environment setup

Secrets have been moved out of tracked source files.

1. Keep your local values in the repo root `.env` file.
2. Use `.env.example` as the template when another machine needs setup.
3. Docker Compose reads the root `.env` automatically.
4. The Spring services now also import `.env` or `../.env`, so local IDE and Maven runs can use the same file.

Sensitive values expected in `.env`:

- `POSTGRES_PASSWORD`
- `REDIS_PASSWORD`
- `RABBITMQ_PASSWORD`
- `JWT_SECRET`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `RAZORPAY_KEY_ID`
- `RAZORPAY_KEY_SECRET`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `MAIL_FROM`
