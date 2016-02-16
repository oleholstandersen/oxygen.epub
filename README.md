# oxygen.epub

EPUB-related extensions for the oXygen XML editor, consisting of a workspace plugin along with frameworks for the main EPUB components (NCX, OPF and XHTML).

The extensions are developed and maintained by Nota, the Danish library for the print-disabled, and are tied to specific requirements and workflows within the organisation. Functions may or not be relevant in other contexts.

## Functionality in Text mode

Extensions in Text mode are handled by the workspace plugin, which adds a custom toolbar for each of the relevant frameworks. The actions accessible from these toolbars vary according to the type of document being edited.

### NCX files and nav.xhtml

* Update navigation: updates navigation documents (nav.ncx and nav.xhtml) to reflect heading hierarchy of content documents in spine

### OPF files

* Concat: Concatenates content documents into a single document (concat.xhtml)
* Split: Splits concat.xhtml into a sequence of content documents
* Import Docx: Imports Docx files as content documents

### XHTML content documents

* Import Docx: replaces the contents of the current document with content from imported Docx files
* Update navigation: updates navigation documents (nav.ncx and nav.xhtml) to reflect heading hierarchy of content documents in spine

## Functionality in Author mode

Extensions in Author mode are provided as custom or generic Author operations embedded in frameworks. (Note that all Text-mode extensions are also available in Author mode.)

### XHTML content documents

#### Heading operations

* Insert headers
* Convert paragraphs to headers
* Shift existing headers

#### List operations

* Insert ordered and unordered lists
* Convert paragraphs to lists
* Convert lists to paragraphs

#### Image operations

* Add images to EPUB archive and insert appropriate XHTML fragments
* Add captions
* Add containers for image descriptions

#### Table operations

* Convert paragraphs to tables
* Convert table cells to paragraphs

#### Repeat operations (EXPERIMENTAL)

* Enable storage of performed operations
* Repeat most recent operation (NOTE: currently does not work for operations performed on a selection)