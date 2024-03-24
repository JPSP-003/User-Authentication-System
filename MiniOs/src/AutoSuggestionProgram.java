import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Token;
import com.google.cloud.language.v1.AnalyzeSyntaxRequest;
import com.google.cloud.language.v1.AnalyzeSyntaxResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AutoSuggestionProgram {
    public static void main(String[] args) {
        try (LanguageServiceClient languageServiceClient = LanguageServiceClient.create()) {
            String prefix = "hel"; // Prefix for autosuggestion

            // Analyze syntax to get suggestions
            Document document = Document.newBuilder().setContent(prefix).setType(Document.Type.PLAIN_TEXT).build();
            AnalyzeSyntaxRequest request = AnalyzeSyntaxRequest.newBuilder().setDocument(document).build();
            AnalyzeSyntaxResponse response = languageServiceClient.analyzeSyntax(request);

            List<String> suggestions = new ArrayList<>();
            for (Token token : response.getTokensList()) {
                if (token.getPartOfSpeech().getTag().equals("NOUN") || token.getPartOfSpeech().getTag().equals("VERB")) {
                    suggestions.add(token.getText().getContent());
                }
            }

            if (suggestions.isEmpty()) {
                System.out.println("No suggestions found for prefix: " + prefix);
            } else {
                System.out.println("Suggestions for prefix '" + prefix + "':");
                for (String suggestion : suggestions) {
                    System.out.println(suggestion);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}