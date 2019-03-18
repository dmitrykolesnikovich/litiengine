package de.gurkenlabs.litiengine.resources;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.util.io.Codec;
import de.gurkenlabs.litiengine.util.io.FileUtilities;

public final class Spritesheets extends ResourcesContainer<Spritesheet> {
  private final Map<String, int[]> customKeyFrameDurations = new ConcurrentHashMap<>();
  private static final Logger log = Logger.getLogger(Spritesheet.class.getName());
  private static final String SPRITE_INFO_COMMENT_CHAR = "#";

  Spritesheets() {
  }

  /**
   * Finds Spritesheets that were previously loaded by any load method or by the
   * sprites.info file.
   * 
   * @param path
   *          The path of the spritesheet.
   * @return The {@link Spritesheet} assotiated with the path or null if not
   *         loaded yet
   */
  @Override
  public Spritesheet get(final String path) {
    if (path == null || path.isEmpty()) {
      return null;
    }

    final String name = FileUtilities.getFileName(path);

    return this.getResources().getOrDefault(name, null);
  }

  @Override
  public Spritesheet get(String resourceName, boolean forceLoad) {
    return this.get(resourceName);
  }

  public int[] getCustomKeyFrameDurations(final String name) {
    return this.customKeyFrameDurations.getOrDefault(FileUtilities.getFileName(name), new int[0]);
  }

  public int[] getCustomKeyFrameDurations(final Spritesheet sprite) {
    return getCustomKeyFrameDurations(sprite.getName());
  }

  public Spritesheet load(final BufferedImage image, final String path, final int spriteWidth, final int spriteHeight) {
    return new Spritesheet(image, path, spriteWidth, spriteHeight);
  }

  public Spritesheet load(final ITileset tileset) {
    if (tileset == null || tileset.getImage() == null) {
      return null;
    }

    if (tileset.getImage().getAbsoluteSourcePath() == null) {
      return null;
    }

    return new Spritesheet(Resources.images().get(tileset.getImage().getAbsoluteSourcePath(), true), tileset.getImage().getSource(), tileset.getTileDimension().width, tileset.getTileDimension().height);
  }

  public Spritesheet load(final SpritesheetResource info) {
    Spritesheet sprite = null;
    if (info.getImage() == null || info.getImage().isEmpty()) {
      log.log(Level.SEVERE, "Sprite {0} could not be loaded because no image is defined.", new Object[] { info.getName() });
      return null;
    } else {
      sprite = load(Codec.decodeImage(info.getImage()), info.getName(), info.getWidth(), info.getHeight());
    }

    if (info.getKeyframes() != null && info.getKeyframes().length > 0) {
      customKeyFrameDurations.put(sprite.getName(), info.getKeyframes());
    }

    return sprite;
  }

  /**
   * The sprite info file must be located under the
   * GameInfo#getSpritesDirectory() directory.
   *
   * @param spriteInfoFile
   *          The path to the sprite info file.
   * @return A list of spritesheets that were loaded from the info file.
   */
  public List<Spritesheet> loadFrom(final String spriteInfoFile) {

    final ArrayList<Spritesheet> sprites = new ArrayList<>();
    final InputStream fileStream = Resources.get(spriteInfoFile);
    if (fileStream == null) {
      return sprites;
    }

    try (BufferedReader br = new BufferedReader(new InputStreamReader(fileStream))) {
      String line;
      while ((line = br.readLine()) != null) {

        if (line.isEmpty() || line.startsWith(SPRITE_INFO_COMMENT_CHAR)) {
          continue;
        }

        final String[] parts = line.split(";");
        if (parts.length == 0) {
          continue;
        }

        final List<String> items = Arrays.asList(parts[0].split("\\s*,\\s*"));
        if (items.size() < 3) {
          continue;
        }

        getSpriteSheetFromSpriteInfoLine(FileUtilities.getParentDirPath(spriteInfoFile), sprites, items, parts);
      }

      log.log(Level.INFO, "{0} spritesheets loaded from {1}", new Object[] { sprites.size(), spriteInfoFile });
    } catch (final IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return sprites;
  }

  public Spritesheet load(final String path, final int spriteWidth, final int spriteHeight) {
    return new Spritesheet(Resources.images().get(path, true), path, spriteWidth, spriteHeight);
  }

  @Override
  public Spritesheet remove(final String path) {
    Spritesheet spriteToRemove = super.remove(path);
    customKeyFrameDurations.remove(path);
    return spriteToRemove;
  }

  public void update(final SpritesheetResource info) {
    if (info == null || info.getName() == null) {
      return;
    }

    final String spriteName = info.getName();

    Spritesheet spriteToRemove = this.remove(spriteName);

    if (spriteToRemove != null) {
      customKeyFrameDurations.remove(spriteName);
      if (info.getHeight() == 0 && info.getWidth() == 0) {
        return;
      }

      load(info);
    }
  }

  private void getSpriteSheetFromSpriteInfoLine(String baseDirectory, ArrayList<Spritesheet> sprites, List<String> items, String[] parts) {
    try {
      final String name = baseDirectory + items.get(0);

      final int width = Integer.parseInt(items.get(1));
      final int height = Integer.parseInt(items.get(2));

      final Spritesheet sprite = load(name, width, height);
      sprites.add(sprite);
      if (parts.length >= 2) {
        final List<String> keyFrameStrings = Arrays.asList(parts[1].split("\\s*,\\s*"));
        if (!keyFrameStrings.isEmpty()) {
          final int[] keyFrames = new int[keyFrameStrings.size()];
          for (int i = 0; i < keyFrameStrings.size(); i++) {
            final int keyFrame = Integer.parseInt(keyFrameStrings.get(i));
            keyFrames[i] = keyFrame;
          }

          customKeyFrameDurations.put(sprite.getName(), keyFrames);
        }
      }
    } catch (final NumberFormatException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  @Override
  protected Spritesheet load(URL resourceName) {
    return null;
  }
}
