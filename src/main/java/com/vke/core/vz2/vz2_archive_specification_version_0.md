# VZ2 Archive Format Specification
(Refined by Google Gemini, the format itself is handmade i just cba to write proper doc lmao)

**Version:** 0  
**Byte Order:** Little-Endian (LSB First)  
**File Extension:** `.vz2`  
**Primitive Types:**  
* `u8`, `u16`, `u32`, `u64`: Unsigned integers of 8, 16, 32, and 64 bits.  
* `PTR`: An absolute file offset stored as a `u64`, measured in bytes from byte 0 of the file. A value of `0x0000000000000000` represents a `NULL` pointer.

---

## 1. Overview & Mechanics

### 1.1 In-Place Modifications & Garbage Management
The VZ2 format is designed for random-access file operations (`RandomAccessFile`). It allows append-only modifications, in-place entry deletions, and chunk recycling without requiring a full archive rebuild.
* Freed byte regions (from deleted strings, abandoned `FTAB` tables, or invalidated `FDATA` chunks) are tracked in a global linked list of `MEMBLOCK` structures starting from `HEADER.pMEMBLOCK`.
* Before expanding the physical file during a write, implementations **MUST** search the `MEMBLOCK` chain for a free region capable of housing the requested payload size (first-fit or best-fit strategy).

### 1.2 Hashes & Component Resolution
Path components are matched using 64-bit forward and reverse FNV-1a hashes.
* **Hash Constants:** 
  * Offset Basis: `0xcbf29ce484222325L`
  * Magic Prime: `0x00000100000001b3L`
* **Fast Lookup:** Implementations may match entries purely via dual FNV-1a hash pairs (`hash` and `revHash`). String verification against `pName`/`sName` is optional for performance, as 128-bit combined hash collisions are treated as statistically negligible.

#### Reference FNV-1a Implementation (Java)
```java
public class VZ2Hash {
    private static final long MAGIC_PRIME = 0x00000100000001b3L;

    public static long FNV1a(byte[] bytes) {
        return FNV1a(bytes, false);
    }

    public static long FNV1a(byte[] bytes, boolean reverse) {
        long hash = 0xcbf29ce484222325L;
        if (!reverse) {
            for (byte b : bytes) {
                hash = (hash ^ (b & 0xFF)) * MAGIC_PRIME;
            }
        } else {
            for (int i = bytes.length - 1; i >= 0; i--) {
                byte b = bytes[i];
                hash = (hash ^ (b & 0xFF)) * MAGIC_PRIME;
            }
        }
        return hash;
    }
}
```

---

## 2. Structure Specifications

### 2.1 HEADER (File Header)
Located strictly at offset `0x0000000000000000`. Total size: 25 bytes.

| Offset | Field       | Type  | Description                                                            |
|:-------|:------------|:------|:-----------------------------------------------------------------------|
| `0x00` | `magic`     | `u32` | Constant `0x565A4949` (ASCII `"IIZV"` in LSB layout / `"VZII"` magic). |
| `0x04` | `version`   | `u8`  | Format version. Must be `0x00`.                                        |
| `0x05` | `pFTAB`     | `PTR` | Pointer to the root directory's first `FTAB` chunk.                    |
| `0x0D` | `pMEMBLOCK` | `PTR` | Pointer to the head of the free memory block list (`NULL` if none).    |

---

### 2.2 FTAB (File Table Chunk)
Represents a segment of a directory layer. Fixed allocation for **16** entry slots, regardless of active entries.

```
|-----------------------------------------------------------------------|
| nEntries (u6) + Reserved (u2) | pParent (PTR)                       |
|-----------------------------------------------------------------------|
| FTAB-ENTRY[0]                                                         |
| FTAB-ENTRY[1]                                                         |
| ...                                                                   |
| FTAB-ENTRY[15] (Fixed 16 slots allocated physically)                  |
|-----------------------------------------------------------------------|
| pNext (PTR)                                                           |
|-----------------------------------------------------------------------|
```

| Field      | Type             | Description                                                                                           |
|:-----------|:-----------------|:------------------------------------------------------------------------------------------------------|
| `nEntries` | `u6`             | Active count of valid entries currently stored in this `FTAB` chunk ($0 \le nEntries \le 16$).        |
| `reserved` | `u2`             | Reserved for alignment/future expansion. Must be `0x0000`.                                            |
| `pParent`  | `PTR`            | Pointer to the parent `FTAB` block of the enclosing directory layer (`NULL` for root).                |
| `entries`  | `FTAB-ENTRY[16]` | Fixed array of 64 `FTAB-ENTRY` slots. Only indices $0$ through $nEntries - 1$ contain active entries. |
| `pNext`    | `PTR`            | Pointer to the next `FTAB` chunk on the **same directory layer** (`NULL` if terminal chunk).          |

#### Entry Removal Rules (`Swap-Remove`)
1. Active entries occupy indices $0 \dots nEntries - 1$ sequentially.
2. To delete an entry at active index $i$:
   * Copy `entries[nEntries - 1]` over `entries[i]`.
   * Set the `fInvalid` bit on the old slot `entries[nEntries - 1]`.
   * Decrement `nEntries` by 1.
3. If an `FTAB` block's `nEntries` drops to 0 and it is not the root node, it may be unlinked from `pNext` and its region added to `pMEMBLOCK`.

---

### 2.3 FTAB-ENTRY (Directory/File Entry Slot)
Total size per slot: **81 bytes**.

| Field              | Type     | Description                                                                                                                                                      |
|:-------------------|:---------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `flags`            | `u8`     | Bitfield flags (see **Flags Layout** below).                                                                                                                     |
| `hash`             | `u64`    | 64-bit FNV-1a hash of component name.                                                                                                                            |
| `revHash`          | `u64`    | 64-bit FNV-1a hash of reversed component name.                                                                                                                   |
| `pName`            | `PTR`    | Pointer to a null-terminated UTF-8 name string. Must be `0x0` if `fShortName == 1`.                                                                              |
| `sName`            | `u8[24]` | Inline UTF-8 string buffer. Active when `fShortName == 1`.                                                                                                       |
| `uncompressedSize` | `u64`    | Uncompressed total byte size of file payload. Must be `0` for directory components.                                                                              |
| `pPtr`             | `PTR`    | **Directory (`fCompType == 1`):** Pointer to first child `FTAB` chunk.<br>**File (`fCompType == 0`):** Pointer to first `FDATA` chunk (`NULL` for 0-byte files). |

#### FTAB-ENTRY `flags` Layout (u8)
* `Bit 0` (`fInvalid`): `1` = Slot is deleted/inactive; `0` = Active entry.
* `Bit 1` (`fCompType`): `0` = File component; `1` = Directory component.
* `Bit 2` (`fShortName`): `1` = Name is $\le 24$ bytes and stored directly inside `sName`; `0` = Name stored externally at `pName`.
* `Bits 3–7`: Reserved. Must be set to `0`.

#### `sName` Termination Rules
* Length $< 24$ bytes: Must end with a `0x00` null-terminator.
* Length $== 24$ bytes: Exactly 24 bytes of characters; no null terminator is present or required.

---

### 2.4 FDATA (File Data Chunk Header & Payload)
Contains raw or DEFLATE-compressed payload data. A single file's payload consists of one or more `FDATA` chunks connected via `pNext`.

| Field         | Type              | Description                                                                                                           |
|:--------------|:------------------|:----------------------------------------------------------------------------------------------------------------------|
| `flags`       | `u8`              | Bitfield flags (see **Flags Layout** below).                                                                          |
| `pNext`       | `PTR`             | Absolute pointer to next `FDATA` chunk in this stream sequence (`NULL` if terminal chunk).                            |
| `ord`         | `u64`             | Non-consecutive, strictly ascending ordering key used to sort chunks into correct sequence.                           |
| `adler32`     | `u32`             | Adler-32 checksum of the **uncompressed** payload within this specific chunk.                                         |
| `nData`       | `u32`             | Total uncompressed length of payload represented across this chunk (including continuation parts if `fPartial == 1`). |
| `nDataLength` | `u32`             | Exact byte length of the trailing `data` array following this header.                                                 |
| `data`        | `u8[nDataLength]` | Payload byte stream.                                                                                                  |

#### FDATA `flags` Layout (u8)
* `Bit 0` (`fInvalid`): `1` = Chunk is orphaned/freed and can be reclaimed; `0` = Active data chunk.
* `Bit 1` (`fPartial`): `1` = Payload was split across physical boundaries during a partial write; `0` = Complete chunk payload.
* `Bit 2` (`fNoCompress`): `1` = Payload in `data` is uncompressed raw bytes; `0` = Payload is DEFLATE compressed.
* `Bits 3–7`: Reserved. Must be set to `0`.

#### Partial Chunks (`fPartial == 1`)
1. `nDataLength` includes the **entire size** of the `data` array, including its trailing 8-byte continuation pointer.
2. The last 8 bytes of `data` (`data[nDataLength - 8 ... nDataLength - 1]`) are reinterpreted as an `PTR` pointing to the next partial continuation block.
3. **DEFLATE Streaming Rule:** 
   * A chain of partial chunks (`fPartial == 1`) forms a **SINGLE** uninterrupted DEFLATE stream across boundaries.
   * Distinct `FDATA` chunks with `fPartial == 0` represent independent, self-contained DEFLATE streams.

---

### 2.5 MEMBLOCK (Free Space Allocator)
Linked list node tracking recycled, available memory regions in the file. Size: **24 bytes**.

| Field    | Type  | Description                                                         |
|:---------|:------|:--------------------------------------------------------------------|
| `pBlock` | `PTR` | Absolute offset to start of recyclable free byte block.             |
| `size`   | `u64` | Length of available block in bytes.                                 |
| `pNext`  | `PTR` | Pointer to next `MEMBLOCK` node in chain (`NULL` if terminal node). |

---

## 3. Operations & Disambiguation Rules

1. **String Storage:** `pName` target strings **MUST** always end in `0x00`. When deleting an entry with an external `pName`, its file span must be added to `pMEMBLOCK`.
2. **Chunk Reordering:** Implementations **MUST** sort retrieved `FDATA` chunks by their `ord` field in ascending numeric order prior to decompressing/concatenating payload streams.
3. **Space Allocation Strategy:** When writing new `FTAB`, `FDATA`, `MEMBLOCK`, or string data, implementations **MUST** search `pMEMBLOCK` for an available block of `size >= required_bytes` before expanding the physical archive file (`EOF`).