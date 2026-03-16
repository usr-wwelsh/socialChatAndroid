# socialChat Android

An Android client for [socialChat](https://github.com/usr-wwelsh/socialChat) — a decentralized, self-hostable social media platform. No central authority. No corporate servers. Just people running their own communities.

## What is socialChat?

socialChat is open-source social media infrastructure that anyone can host. The Android app lets you connect to any socialChat instance — including ones you run yourself.

- Post, comment, and follow people on your own server
- Chat in real-time with friends and communities
- Full control over your data and who hosts it
- Switch between servers from the login screen

## Self-Hosting

Run your own socialChat server and connect this app to it in seconds.

1. Deploy the server: [github.com/usr-wwelsh/socialChat](https://github.com/usr-wwelsh/socialChat)
2. Open the app → tap the server selector on the login screen
3. Add your server URL (e.g. `https://social.yourdomain.com`)
4. Sign up and you're live on your own platform

Your instance, your rules, your data.

## Features

- Decentralized — connect to any socialChat server
- Real-time chat and messaging via WebSockets
- Posts, comments, profiles, and friend requests
- Persistent server list — switch instances without reinstalling
- Brutalist dark UI

## Building

```bash
git clone https://github.com/usr-wwelsh/socialChatAndroid
cd socialChatAndroid
./gradlew assembleDebug
```

Requires Android Studio Hedgehog or newer, min SDK 26.

## License

MIT License — see [LICENSE](LICENSE)
