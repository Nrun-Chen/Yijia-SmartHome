<template>
  <view class="container">
   <iframe
     src="http://localhost:8080/chat/replace-with-chat-id?mode=mobile&header=0"
     style="width: 100%; height: 100%;"
     frameborder="0"
     allow="microphone">
   </iframe>

  </view>
</template>

<script>
export default {
  name: "AiPage"
}
</script>

<style>
.container {
  width: 100%;
  height: 100vh;
}
</style>
