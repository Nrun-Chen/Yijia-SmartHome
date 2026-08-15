package com.cqu.smarthome.demo.pojo.dto;

public class ChatResponse {
    private String response;
    private String model;
    private Usage usage;

    // Getters and Setters
    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Usage getUsage() {
        return usage;
    }

    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    public static class Usage {
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;

        // Getters and Setters
        public int getPromptTokens() {
            return promptTokens;
        }

        public void setPromptTokens(int promptTokens) {
            this.promptTokens = promptTokens;
        }

        public int getCompletionTokens() {
            return completionTokens;
        }

        public void setCompletionTokens(int completionTokens) {
            this.completionTokens = completionTokens;
        }

        public int getTotalTokens() {
            return totalTokens;
        }

        public void setTotalTokens(int totalTokens) {
            this.totalTokens = totalTokens;
        }
    }
}