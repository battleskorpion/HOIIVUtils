# HOI4Utils PDX Parsing Architecture - Complete Documentation

## **PDX = Paradox Interactive Clausewitz Engine Modding/Scripting Language**

This is the comprehensive documentation for how Hearts of Iron IV mod files get parsed through the PDX script hierarchy, written for future reference.

## **Core PDX Script Hierarchy**

### **1. PDXScript[V] (Root Trait)**
- **Purpose**: Base trait for all Paradox script types
- **Key Methods**:
  - `set(obj: V)`: Set value
  - `loadPDX(expression: Node)`: Load from parsed node
  - `isValidIdentifier(node: Node)`: Check if node matches expected identifiers
  - `toScript: String`: Generate script output
  - `updateNodeTree()`: Rebuild underlying Node structure

### **2. AbstractPDX[T] (Base Implementation)**
- **Purpose**: Common implementation for all PDX scripts
- **Key Features**:
  - Manages `pdxIdentifiers: List[String]` (valid identifier names)
  - Filters nodes in `loadPDX(expressions: Iterable[Node])`:
    ```scala
    expressions.filter(this.isValidIdentifier).foreach(expression =>
      loadPDX(expression)
      remaining -= expression
    )
    ```
  - **Critical**: Only processes nodes with matching identifiers, others are returned as "remaining"

### **3. StructuredPDX (Complex Objects)**
- **Purpose**: PDX objects with child properties (like FocusTree, Focus)
- **Key Mechanism** (lines 34-39):
  ```scala
  case l: ListBuffer[Node] =>
    var remainingNodes = Iterable.from(l)
    for pdxScript <- childScripts do
      remainingNodes = pdxScript.loadPDX(remainingNodes)
    badNodesList = remainingNodes
  ```
- **Process**: Each child script processes its valid nodes, unprocessed nodes go to `badNodesList`

### **4. MultiPDX[T] (Collections)**
- **Purpose**: Multiple instances of same type (e.g., multiple focuses in a focus tree)
- **Suppliers**:
  - `simpleSupplier`: For simple values (e.g., `icon = "filename"`)
  - `blockSupplier`: For block structures (e.g., `focus = { ... }`)
- **Loading**: Creates new instances via suppliers for each matching node

### **5. Simple PDX Types**
- **StringPDX**: String values (`id = "focus_name"`)
- **IntPDX**: Integer values (`x = 5`)
- **DoublePDX**: Double values (`cost = 10.0`)
- **BooleanPDX**: Boolean values (`available_if_capitulated = yes`)

## **How Focus Trees Get Parsed**

### **Step 1: File Loading**
```scala
// FocusTree constructor (line 42)
loadPDX(file)
```

### **Step 2: Parser Processing**
1. **Parser** reads file → creates root **Node** tree
2. **FocusTree** extends **StructuredPDX("focus_tree")**
3. Root node should have identifier "focus_tree"

### **Step 3: Child Script Processing**
```scala
// FocusTree.childScripts (line 47)
ListBuffer(id, country, focuses)
```

**Processing Order**:
1. **`id: StringPDX("id")`** - processes `id = California`
2. **`country: FocusTreeCountryPDX`** - processes `country = { ... }`
3. **`focuses: MultiPDX[Focus]`** - processes all `focus = { ... }` blocks

### **Step 4: Focus Collection**
- **MultiPDX[Focus]** has `blockSupplier = Some(() => new Focus(this))`
- For each `focus = { ... }` node:
  1. Creates new `Focus(focusTree)` instance
  2. Calls `focus.loadPDX(node)` to populate focus properties
  3. Adds focus to internal `pdxList: ListBuffer[Focus]`

### **Step 5: Focus Property Loading**
Each **Focus** extends **StructuredPDX("focus")** with child scripts:
```scala
ListBuffer(id, icon, x, y, prerequisites, mutuallyExclusive,
          relativePositionFocus, cost, availableIfCapitulated,
          cancelIfInvalid, continueIfInvalid, ai_will_do)
```

## **Why 4 Focuses Were "Missing" from California.txt**

### **Root Cause: Commented Focus Blocks**
The file contains **4 commented-out focus blocks**:
```paradox
# focus = {
# 	id = SCA_silver_legions
# 	...
# }
```

### **Parser Behavior**:
1. **Raw grep count**: `grep "focus = {"` finds **230** (includes comments)
2. **Parser processing**: **Tokenizer** treats `#` as comments, so commented blocks are **ignored**
3. **Actual parsed focuses**: **226** (230 - 4 = 226)

### **Validation**:
- Found exactly 4 commented focus blocks: `grep -c "# focus = {" california.txt` = 4
- Parser correctly ignores comments during tokenization
- 230 - 4 = 226 matches actual parsed count

## **Complete Parsing Flow Diagram**

```
File Input (california.txt)
    ↓
Tokenizer (ignores # comments)
    ↓
Parser → Root Node Tree
    ↓
FocusTreeFile.loadPDX(rootNode)
    ↓
StructuredPDX processes child scripts:
    ├── id.loadPDX() → finds "id = California"
    ├── country.loadPDX() → finds "country = {...}"
    └── focuses.loadPDX() → finds all "focus = {...}" blocks
                            (excludes 4 commented blocks)
         ↓
    MultiPDX[Focus] creates Focus instances:
         └── For each focus = {...}:
             1. new Focus(focusTree)
             2. Focus.loadPDX(focusNode)
             3. Focus loads its child properties
             4. Add to focuses.pdxList
    ↓
Final Result: FocusTree with 226 Focus objects
```

## **Key Architecture Insights**

### **1. Identifier-Based Filtering**
- Every PDX script has `pdxIdentifiers` that define what nodes it accepts
- **StructuredPDX** delegates to child scripts in sequence
- **Unmatched nodes** go to `badNodesList` (preserved for script generation)

### **2. Node Preservation**
- Original node structure is preserved for `toScript()` generation
- `updateNodeTree()` rebuilds nodes from current state
- **Comments and unrecognized blocks are preserved** in output

### **3. Supplier Pattern**
- **MultiPDX** uses suppliers to create new instances dynamically
- Handles both simple values and complex block structures
- **Focus creation**: `blockSupplier = Some(() => new Focus(this))`

### **4. Error Handling**
- **UnexpectedIdentifierException**: Node identifier not in `pdxIdentifiers`
- **NodeValueTypeException**: Node value type doesn't match expected type
- **Graceful degradation**: Invalid nodes preserved rather than failing

### **5. Global State Management**
- **FocusTree constructor automatically calls `FocusTree.add(this)`**
- Creates singleton collection for UI access
- **Test issue**: Multiple parsing runs accumulate instances

## **Summary: The 4 Missing Focuses**

The **"missing" 4 focuses** were never actually missing from the parser's perspective. They are **commented-out focus blocks** that:

1. **Appear in grep counts** (230 total `focus = {` patterns)
2. **Are correctly ignored by the parser** (comments filtered by tokenizer)
3. **Result in accurate parsed count** (226 actual focus objects)

The parser is working exactly as intended - it correctly ignores commented code blocks while preserving the file structure for script regeneration.

## **HOI4 PDX Class Architecture: File vs Child Block Classes**

The HOI4 codebase follows a consistent pattern where each game system has **two types of classes**:

### **FILE CLASSES** (Represent entire mod files)
These classes represent complete `.txt` files and manage top-level file structure:

| **Package** | **File Class**     | **Represents** | **File Type** |
|-------------|--------------------|----------------|---------------|
| `hoi4.focus` | **FocusTreeFile**  | Entire focus tree file | `common/national_focus/*.txt` |
| `hoi4.idea` | **IdeaFile**       | Entire ideas file | `common/ideas/*.txt` |
| `hoi4.country` | **CountryFile**    | Country definition file | `common/countries/*.txt` |
| `hoi4.units` | **OrdersOfBattle** | OOB file | `history/units/*.txt` |

**Characteristics of File Classes:**
- Extend `StructuredPDX` with the root identifier (e.g., `"focus_tree"`, `"ideas"`)
- Manage collections of child blocks via `MultiPDX` or `CollectionPDX`
- Handle file I/O operations (`loadPDX(file: File)`)
- Maintain static registries for all loaded instances
- Have companion objects with `read()`, `clear()`, `add()` methods

### **CHILD BLOCK CLASSES** (Represent blocks within files)
These classes represent individual blocks/definitions within the files:

| **Package** | **Child Block Class** | **Represents** | **Parent File Class** |
|-------------|----------------------|----------------|----------------------|
| `hoi4.focus` | **Focus** | Individual focus block | FocusTree |
| `hoi4.idea` | **Idea** | Individual idea block | IdeaFile |
| `hoi4.country` | **Character** | Character definition | Country |
| `hoi4.units` | **DivisionTemplate** | Division template | OrdersOfBattle |
| `hoi4.effect` | **Effect** | Game effect block | Various |
| `hoi4.modifier` | **Modifier** | Modifier definition | Various |

**Characteristics of Child Block Classes:**
- Extend `StructuredPDX` with specific block identifiers (e.g., `"focus"`, idea names)
- Contain game-specific properties as PDX fields
- Often have references back to parent file class
- Implement game logic and validation rules

### **Example File Structure Mapping:**

#### **Focus Tree File** (`california.txt`):
```paradox
focus_tree = {                    # ← FocusTree class
    id = California               #   ├── id: StringPDX("id")
    country = { ... }             #   ├── country: FocusTreeCountryPDX
    focus = {                     #   └── focuses: MultiPDX[Focus]
        id = some_focus           #       └── Focus class
        x = 5                     #           ├── id: StringPDX("id")
        y = 1                     #           ├── x: IntPDX("x")
        # ... other focus props   #           └── (other properties)
    }
    focus = { ... }               #       └── Focus class (another)
}
```

#### **Ideas File** (`california.txt`):
```paradox
ideas = {                         # ← IdeaFile class
    country = {                   #   └── countryIdeas: CollectionPDX[Idea]
        great_depression = {      #       └── Idea class
            picture = depression  #           ├── modifiers: CollectionPDX[Modifier]
            modifier = { ... }    #           └── removalCost: DoublePDX("cost")
        }
        war_economy = { ... }     #       └── Idea class (another)
    }
}
```

### **Complete HOI4 Class Categorization:**

#### **📁 FILE CLASSES (Manage entire files):**
- `FocusTree` - National focus files
- `IdeaFile` - Ideas files
- `Country` - Country definition files
- `OrdersOfBattle` - Military unit organization files
- `Defines` - Game defines files

#### **📄 CHILD BLOCK CLASSES (Blocks within files):**
- `Focus` - Individual focuses within focus trees
- `Idea` - Individual ideas within idea files
- `Character` - Characters within country files
- `DivisionTemplate` - Division templates within OOB files
- `Technology` - Individual technologies
- `Equipment` - Equipment definitions
- `Effect` - Game effects (add_manpower, etc.)
- `Modifier` - Game modifiers (stability_factor, etc.)

#### **🔧 UTILITY CLASSES (Supporting functionality):**
- `CountryTag` - Country identifiers (GER, USA, etc.)
- `Scope` - Game scopes (country, state, etc.)
- `Parameter` - Effect parameters
- `CustomTooltip` - UI tooltips

### **Loading Pattern:**
1. **File Class** loads entire file and creates root structure
2. **File Class** uses `MultiPDX` or `CollectionPDX` to manage child collections
3. **Child Block Classes** are created for each matching block
4. **Child Block Classes** load their specific properties and game logic

This architecture provides clear separation between file management and game object representation, making the codebase maintainable and extensible.