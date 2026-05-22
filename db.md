```{mermaid}
---
title: DB
---

erDiagram
direction TB

    user ||--o{ location : ""
    letter ||--o{ location: ""
    user ||--o{ letter: "宛先"
    user ||--o{ letter: "差出"

    user {
    	string user_name PK "ユーザー名"
    }
    letter {
        int letter_id PK "手紙ID"
        string for FK "宛先ユーザー名"
        string from FK "差出人ユーザー名"
        string sentence "手紙内容"
        boolean is_survival "到達したかどうか"
        json tree "木"
    }
    location {
        int letter_user_id PK "手紙とユーザーの組合せID"
        int letter_id FK "手紙ID"
        string user_name FK "ユーザー名"
        double latitude "緯度"
        double longitude "経度"
    }
```
