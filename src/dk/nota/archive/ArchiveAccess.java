package dk.nota.archive;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;

public class ArchiveAccess {
	
	private Path archivePath;
	
	public ArchiveAccess(URI archiveUri) {
		archivePath = Paths.get(archiveUri);
	}
	
	public File backupArchive() throws IOException {
		Path backupArchivePath = archivePath.resolveSibling(archivePath
				.getFileName() + ".bak");
		return Files.copy(archivePath, backupArchivePath,
				StandardCopyOption.REPLACE_EXISTING).toFile();
	}
	
	public LinkedList<URI> copyFilesToArchiveFolder(String internalFolder,
			boolean replace, File... files) throws IOException {
		LinkedList<URI> copiedFileUris = new LinkedList<URI>();
		try (FileSystem archiveFileSystem = getArchiveAsFileSystem()) {
			Path basePath = archiveFileSystem.getPath(internalFolder);
			Files.createDirectories(basePath);
			for (int i = 0; i < files.length; i++) {
				String fileName = files[i].getName();
				Path filePath = basePath.resolve(fileName);
				if (!replace)
					// If files should not be replaced, add a random suffix
					// prior to creating the file
					while (Files.exists(filePath)) {
						// TODO: Find a prettier solution - just decide on a
						// suffix format indicating a file copy and increment
						// as needed
						int suffix = new Random().nextInt(Integer.MAX_VALUE);
						fileName = fileName.replaceFirst("^(.+)(\\..*?)$",
										"$1-" + suffix + "$2");
						filePath = basePath.resolve(fileName);
					}
				copiedFileUris.add(Files.copy(files[i].toPath(), filePath,
						StandardCopyOption.REPLACE_EXISTING).toUri());
			}
		} catch (IOException e) {
			throw e;
		}
		return copiedFileUris;
	}
	
	public FileSystem getArchiveAsFileSystem() throws IOException {
		return FileSystems.newFileSystem(archivePath, null);
	}
	
	public File getArchiveFile() {
		return archivePath.toFile();
	}
	
	public Path getArchivePath() {
		return archivePath;
	}
	
	public URI getArchiveInternalUri() {
		return URI.create("zip:" + archivePath.toUri() + "!/");
	}
	
	public LinkedList<Path> getDirectoryContents(String directoryPath)
			throws IOException {
		try (FileSystem archiveFileSystem = getArchiveAsFileSystem()) {
			return getDirectoryContents(archiveFileSystem, directoryPath);
		}
	}
	
	public LinkedList<Path> getDirectoryContents(FileSystem archiveFileSystem,
			String directoryPath) throws IOException {
		LinkedList<Path> paths = new LinkedList<Path>();
		Files.walk(archiveFileSystem.getPath(directoryPath), 1)
			.sorted()
			.forEach(path -> paths.add(path));
		return paths;
	}
	
	public URI makeArchiveBasedUri(String relativePath) {
		return URI.create(getArchiveInternalUri() + relativePath);
	}
	
	public String relativizeUriToArchive(URI uri) {
		if (!uri.isAbsolute()) throw new IllegalArgumentException(
				String.format("The provided URI (%s) is not absolute", uri));
		String[] components = uri.toString().split("!/");
		if (components.length < 2) throw new IllegalArgumentException(
				String.format("The provided URI (%s) is not an archive URI",
						uri));
		return components[1];
	}
	
	public void serializeNodeToArchive(XdmNode node, URI uri,
			Serializer serializer, FileSystem archiveFileSystem)
			throws IOException, SaxonApiException {
		Path path = archiveFileSystem.getPath(relativizeUriToArchive(uri));
		try (OutputStream outputStream = Files.newOutputStream(path,
				StandardOpenOption.CREATE)) {
			serializer.setOutputStream(outputStream);
			serializer.serializeNode(node);
		} finally {
			serializer.close();
		}
	}
	
	public void serializeNodesToArchive(Map<URI,XdmNode> nodes,
			Serializer serializer, FileSystem archiveFileSystem)
			throws IOException, SaxonApiException {
		for (Entry<URI,XdmNode> entry : nodes.entrySet())
			serializeNodeToArchive(entry.getValue(), entry.getKey(),
					serializer, archiveFileSystem);
	}

}
