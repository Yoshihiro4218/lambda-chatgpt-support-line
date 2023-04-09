# LINE で OpenAI 社の API と連携するチャットボットを作ってみた (ChatGPT)

## 構成とやりとりの流れ

<div align="center">
<img src="material_for_lt/chat-gpt-support-line.drawio.png" alt="構成とやりとりの流れ" title="構成とやりとりの流れ">
</div>

1. LINE 公式アカウントの `MessagingAPI` を用いて AWS の `API Gateway` に紐付いている URL に Webhook イベントを送信する
2. AWS `Lambda` に連携
3. AWS `DynamoDB` に会話を保存 + 過去の会話履歴を新しい方から適当な分だけ取得
4. OpenAI 社の API を叩いて返答を得る
5. AWS `DynamoDB` に返答を保存
6. `MessagingAPI` の `Reply API` を叩いて返事を LINE に送信

## 1. MessagingAPI で Webhook イベント送信 -> 2. Lambda に連携

LINE 公式アカウントを作る
![](material_for_lt/1.png)

`Messaging API設定` にて AWS の `API Gateway` に紐付いている URL を Webhook URL に設定し、利用を有効にする
![](material_for_lt/2.png)

LINE 公式アカウントとのトークでメッセージを送信
![](material_for_lt/3.png)

この発言内容が API Gateway 経由で Lambda に連携される

## 3. DynamoDB に会話を保存 + 過去の会話履歴を取得

Lambda で受け取った発言内容をまずは DynamoDB に保存 + 過去の会話履歴を取得する
![](material_for_lt/4.png)
![](material_for_lt/5.png)

過去の発言を新しい方からいくらか取得する理由は、4.で OpenAI 社の API を叩く際に、AI が会話の文脈を理解できるようにするためです

また、カラムは

- `ID(ランダム文字列)`
- `本文`
- `ロール`
- `発言日時`
- `ユーザーID (LINEユーザーごとの一意の値)`

を設けていますが、一例なので過不足はあるかもしれません  
ロールについては後述します

## 4. OpenAI 社の API と連携

いよいよ OpenAI 社の API を叩きます  
その際必要になるものは主に以下です

- model
- maxTokens
- messages
    - role
    - content

### model

OpenAI 社では用途に応じて様々なモデルを用意してくれています  
以下が主なモデルです

| ベースモデル  |       モデル        | 最大トークン数 |                   特徴                    |
|:--------|:----------------:|--------:|:---------------------------------------:|
| GPT-3.5 | text-davinci-003 |   4,097 |                  最も高性能                  |
| GPT-3.5 |     gpt-3.5-turbo      |   4,096 | davinci より小規模で高速<br>コストも davinci の 1/10 |
| GPT-4       |     gpt-4-32k      |    32,768 |  どの GPT-3.5 モデルよりも能力が高く、より複雑なタスクを実行できる  |
| Codex       |     code-davinci-002    |   8,001 |            自然言語をコードに変換するのが得意            |
| Whisper    |     whisper-1      |       - |   音声認識モデル<br>多言語の音声認識、音声翻訳、言語識別を実行できる   |







