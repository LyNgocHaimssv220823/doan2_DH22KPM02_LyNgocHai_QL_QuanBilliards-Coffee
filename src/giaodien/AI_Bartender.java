package giaodien;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

public class AI_Bartender {
    // Thay bằng API Key từ Groq của bạn
    private static final String API_KEY = "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"; 
    private static final String URL = "https://api.groq.com/openai/v1/chat/completions";

    public static String layHuongDanPhaChe(String tenMon) {
        try {
            JSONObject json = new JSONObject();
            
            // Đổi sang model llama-3.3-70b-versatile (Rất mạnh và ổn định)
            // Hoặc dùng llama-3.1-8b-instant nếu muốn tốc độ nhanh tối đa
            json.put("model", "llama-3.3-70b-versatile"); 
            
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject()
                .put("role", "user")
                .put("content", "Bạn là chuyên gia pha chế cho quán coffee. Hãy hướng dẫn công thức món ăn hoặc thức uống có tên là: " + tenMon + ", với định lượng khoảng 300ml sau khi thành phẩm, dùng định dạng HTML (<b>, <br>, <ul>, <li>)."));
            
            json.put("messages", messages);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject resJson = new JSONObject(response.body());
                return resJson.getJSONArray("choices")
                              .getJSONObject(0)
                              .getJSONObject("message")
                              .getString("content");
            } else {
                return "Lỗi API: " + response.statusCode() + " - " + response.body();
            }
        } catch (Exception e) {
            return "Lỗi kết nối: " + e.getMessage();
        }
    }
}