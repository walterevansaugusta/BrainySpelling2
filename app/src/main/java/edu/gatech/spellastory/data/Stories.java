package edu.gatech.spellastory.data;

import android.content.res.AssetManager;

import org.apache.commons.lang3.NotImplementedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.gatech.spellastory.domain.Category;
import edu.gatech.spellastory.domain.stories.Story;
import edu.gatech.spellastory.domain.stories.StoryBlank;
import edu.gatech.spellastory.domain.stories.StoryLine;
import edu.gatech.spellastory.domain.stories.StoryToken;

public class Stories {

    private Map<String, Story> storyMap;
    private Categories categoriesDb;

    public Stories(Categories categoriesDb, AssetManager assets, String... stories) throws IOException {
        this.categoriesDb = categoriesDb;
        storyMap = readStories(Arrays.asList(stories), assets);
    }

    private Map<String, Story> readStories(List<String> stories, AssetManager assets) throws IOException {
        Map<String, Story> map = new HashMap<>();

        for (String storyName : stories) {
            InputStreamReader reader = new InputStreamReader(assets.open("stories/" + storyName + ".txt"));
            BufferedReader br = new BufferedReader(reader);

            Story story = new Story();
            String line;
            while ((line = br.readLine()) != null) {
                List<StoryToken> storyTokens = parseLine(line);
                story.addAll(storyTokens);
            }

            map.put(storyName, story);
        }

        return map;
    }

    private List<StoryToken> parseLine(String line) {
        List<StoryToken> storyTokens = new ArrayList<>();
        Map<String, List<Category>> blankCategories = new HashMap<>();
        String[] stringTokens = line.split(" ");

        int i = 0;
        while (i < stringTokens.length) {
            String token = stringTokens[i];
            System.out.println(token);

            if (StoryLine.isAudio(token)) {
                i++;

                StringBuilder builder = new StringBuilder();
                while (i < stringTokens.length && isText(stringTokens[i])) {
                    builder.append(stringTokens[i++]);
                }
                String text = builder.toString();

                storyTokens.add(new StoryLine(token, text));
            } else if (StoryBlank.isBlank(token)) {
                String[] blankTokens = token.split("-");
                String identifier = blankTokens[0];

                List<Category> categories;
                if (blankTokens.length > 1) {
                    String[] categoryStrings = blankTokens[1].split(",");
                    categories = categoriesDb.getCategories(categoryStrings);
                    blankCategories.put(identifier, categories);
                } else {
                    categories = blankCategories.get(identifier);
                }

                storyTokens.add(new StoryBlank(identifier, categories));
                i++;
            } else {
//                throw new NotImplementedException("What kind of token is this? " + token);
            }
        }

        return storyTokens;
    }

    private static boolean isText(String string) {
        return !StoryLine.isAudio(string) && !StoryBlank.isBlank(string);
    }

    public Story getStory(String story) {
        return storyMap.get(story);
    }
}
