/*
 * * Copyright 2019 github.com/ReflxctionDev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.regenerato.modern;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.session.ClipboardHolder;
import io.github.regenerato.worldedit.NoSchematicException;
import io.github.regenerato.worldedit.SchematicProcessor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import sun.misc.Unsafe;

import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;

/**
 * Schematic processor for FastAsyncWorldEdit
 */
public class FAWESchematicProcessor extends WESchematicProcessor {

    private static Unsafe UNSAFE; // i dont like it either but we have no other choice

    public FAWESchematicProcessor() {
    }

    private FAWESchematicProcessor(WorldEditPlugin plugin, String name, File directory) {
        super(plugin, name, directory);
    }

    @Override
    public void write(ClipboardHolder selection) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                super.write(selection);
            } catch (EmptyClipboardException e) {
                throw sneakyThrow(e);
            }
        });
    }

    @Override
    public CompletableFuture<EditSession> paste(Location location) {
        CompletableFuture<EditSession> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                super.paste(location);
                future.complete(null);
            } catch (NoSchematicException e) {
                throw sneakyThrow(e);
            }
        });
        return future;
    }

    /**
     * Creates a new instance of the processor
     *
     * @param plugin    Plugin instance
     * @param name      Name of the schematic
     * @param directory The directory of the schematic processor
     * @return The newly created schematic processor
     */
    @Override
    protected SchematicProcessor newInstance(WorldEditPlugin plugin, String name, File directory) {
        return new FAWESchematicProcessor(plugin, name, directory);
    }

    static {
        try {
            Field unsafe = Unsafe.class.getDeclaredField("theUnsafe");
            unsafe.setAccessible(true);
            UNSAFE = (Unsafe) unsafe.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}