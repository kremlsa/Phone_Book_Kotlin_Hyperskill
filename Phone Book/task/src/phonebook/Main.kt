package phonebook

import java.io.File

var phoneBook: MutableList<String> = mutableListOf<String>()
var searches: MutableList<String> = mutableListOf<String>()
var founds = 0
var startTime = 0L
var endTime = 0L
var searchTimeLinear = 0L
var searchTimeJump = 0L
var sortTimeBubble = 0L
var sortTimeQuick = 0L
var searchTimeBin = 0L
var creatingHashTable = 0L
var searchTimeHash = 0L
var tab: phonebook.HashTable<String> = phonebook.HashTable(1000)

fun main() {
    phoneBook = loadFile("C:\\Java_lessons\\directory.txt")
    searches = loadFile("C:\\Java_lessons\\find.txt")

    //Linear Search
    //
    println("Start searching (linear search)...")
    simpleSearch()
    searchTimeLinear = endTime - startTime
    println("Found $founds / ${searches.size} entries. Time taken: ${timeMeasure(searchTimeLinear)}")
    println()
    //
    //Bubble Sort + Jumping Search
    //
    founds = 0

    println("Start searching (bubble sort + jump search)...")
    if (bubbleSort()) {
        startTime = System.currentTimeMillis()
        for (s in searches) {
            if (jumpSearch(s!!) >= 0) {
                founds++
            }
        }
        searchTimeJump = System.currentTimeMillis() - startTime
        println("Found $founds / ${searches.size} entries. ${timeMeasure(sortTimeBubble + searchTimeJump)}")
        println("Sorting time: ${timeMeasure(sortTimeBubble)}")
        println("Searching time: ${timeMeasure(searchTimeJump)}")
    }
    else {
        simpleSearch()
        searchTimeLinear = endTime - startTime
        println("Found $founds / ${searches.size} entries. ${timeMeasure(sortTimeBubble + searchTimeLinear)}")
        //println("Found 500 / ${searches.size} entries. ${timeMeasure(sortTimeBubble + searchTimeLinear)}")
        println("Sorting time: ${timeMeasure(sortTimeBubble)} - STOPPED, moved to linear search")
        println("Searching time: ${timeMeasure(searchTimeLinear)}")
    }
    println()
    //
    // Quick sort + Binary Search
    //
    println("Start searching (quick sort + binary search)...")
    founds = 0
    phoneBook = loadFile("C:\\Java_lessons\\directory.txt")

    startTime = System.currentTimeMillis()
    quickSort(0, phoneBook.size - 1)
    sortTimeQuick = System.currentTimeMillis() - startTime

    startTime = System.currentTimeMillis()
    for (s in searches) {
        val indexBin = binarySearch(s!!, 0, phoneBook.size)
        if (indexBin >= 0) {
            founds++
        }
    }
    searchTimeBin = System.currentTimeMillis() - startTime

    println("Found $founds / ${searches.size} entries. ${timeMeasure(sortTimeQuick + searchTimeBin)}")
    println("Sorting time: ${timeMeasure(sortTimeQuick)}")
    println("Searching time: ${timeMeasure(searchTimeBin)}")
    println()
    //
    // Hash Table
    //
    println("Start searching (hash table)...")
    founds = 0

    startTime = System.currentTimeMillis()
    makeHashTable("C:\\Java_lessons\\directory.txt")
    creatingHashTable = System.currentTimeMillis() - startTime
    startTime = System.currentTimeMillis()
    for (s in searches) {
        val hash = Math.abs(s.hashCode())
        if (tab.get(hash) != null) {
            //System.out.println(tab.get(hash));
            founds++
        }
    }
    searchTimeHash = System.currentTimeMillis() - startTime
    println("Found $founds / ${searches.size} entries. ${timeMeasure(creatingHashTable + searchTimeHash)}")
    println("Creating time: ${timeMeasure(creatingHashTable)}")
    println("Searching time: ${timeMeasure(searchTimeHash)}")
    //
    // End

}

fun timeMeasure(delta: Long): String {
    val seconds = (delta / 1000) % 60
    val minutes = (delta / (1000 * 60) % 60)
    val miliseconds = (delta % 1000)
    return "$minutes min. $seconds sec. $miliseconds ms."
}

fun simpleSearch() {
    startTime = System.currentTimeMillis()
    for (record in phoneBook) {
        if (inSearch(record)) founds++
    }
    endTime = System.currentTimeMillis()
}

fun inSearch(search: String): Boolean {
    for (record in searches) {
        if (search.contains((record + "$").toRegex())) {
            return true
        }
    }
    return false
}

fun loadFile(fileName: String): MutableList<String> {
    var res: MutableList<String> = mutableListOf<String>()
    try {
        val lines = File(fileName).readLines()
        for (line in lines) {
            res.add(line)

        }
    } catch (e: Exception) {
        println("File not found.")
    }
    return res
}

fun bubbleSort(): Boolean {
    val startTimeBubble = System.currentTimeMillis()
    for (i in 0 until phoneBook.size - 1) {
        for (j in 0 until phoneBook.size - i - 1) {
            val note = phoneBook[j]
            val name = note.split(" ".toRegex()).toTypedArray()[1]
            val note1 = phoneBook[j + 1]
            val name1 = note1.split(" ".toRegex()).toTypedArray()[1]
            if (name.compareTo(name1) > 0) {
                val temp = phoneBook[j]
                phoneBook[j] = phoneBook[j + 1]
                phoneBook[j + 1] = temp
            }
        }
        sortTimeBubble = System.currentTimeMillis() - startTimeBubble
        if (sortTimeBubble > searchTimeLinear) {
            return false
        }
    }
    return true
}

fun jumpSearch(target: String): Int {
    val start = System.currentTimeMillis()
    var currentRight = 0
    var prevRight = 0
    if (phoneBook[currentRight] == target) {
        return 0
    }
    val jumpLength = Math.sqrt(phoneBook.size.toDouble()).toInt()
    while (currentRight < phoneBook.size - 1) {
        currentRight = Math.min(phoneBook.size - 1, currentRight + jumpLength)
        val note = phoneBook[currentRight]
        val name = note.split(" ".toRegex()).toTypedArray()[1]
        val target1 = target.split(" ".toRegex()).toTypedArray()[0]
        if (name.compareTo(target1) >= 0) {
            break
        }
        prevRight = currentRight
    }
    val note = phoneBook[currentRight]
    val name = note.split(" ".toRegex()).toTypedArray()[1]
    val target1 = target.split(" ".toRegex()).toTypedArray()[0]
    if (currentRight == phoneBook.size - 1 && target1.compareTo(name) > 0) {
        return -1
    }
    val res: Int = backwardSearch(target, prevRight, currentRight)
    searchTimeJump = System.currentTimeMillis() - start
    return res
}

fun backwardSearch(target: String?, leftExcl: Int, rightIncl: Int): Int {
    for (i in rightIncl downTo leftExcl + 1) {
        val note = phoneBook[i]
        val name = note.split(" ".toRegex()).toTypedArray()[1]
        if (note.contains(target!!)) {
            return i
        }
    }
    return -1
}

fun quickSort(left: Int, right: Int) {
    if (left < right) {
        val pivotIndex = partition(left, right)
        quickSort(left, pivotIndex - 1)
        quickSort(pivotIndex + 1, right)
    }
}

fun partition(left: Int, right: Int): Int {
    val pivot1: String = phoneBook.get(right)
    val pivot = pivot1.substring(pivot1.indexOf(' ') + 1)
    var partitionIndex = left
    for (i in left until right) {
        val name: String = phoneBook.get(i).substring(phoneBook.get(i).indexOf(' ') + 1)
        if (abcOrder(name, pivot)) {
            swap(i, partitionIndex)
            partitionIndex++
        }
    }
    swap(partitionIndex, right)
    return partitionIndex
}

fun swap(i: Int, j: Int) {
    val temp: String = phoneBook.get(i)
    phoneBook.set(i, phoneBook.get(j))
    phoneBook.set(j, temp)
}

fun abcOrder(first: String, second: String): Boolean {
    for (k in 0 until Math.min(first.length, second.length)) {
        if (first.toLowerCase()[k] < second.toLowerCase()[k]) {
            return true
        } else if (first.toLowerCase()[k] > second.toLowerCase()[k]) {
            return false
        }
    }
    return first.length <= second.length
}

fun binarySearch(elem: String, left: Int, right: Int): Int {
    var left = left
    var right = right
    while (left <= right) {
        val mid = left + (right - left) / 2
        val name: String = phoneBook.get(mid).substring(phoneBook.get(mid).indexOf(' ') + 1)
        if (name == elem) {
            return mid
        } else if (abcOrder(name, elem) == false) {
            right = mid - 1
        } else {
            left = mid + 1
        }
    }
    return -1
}

internal class TableEntry<T>(val key: Int, val value: T) {
    private val removed = false

}

class HashTable<T>(private var size: Int) {
    private var table: Array<TableEntry<*>?>
    //private var tabletemp: Array<TableEntry<*>>
    fun put(key: Int, value: T): Boolean {
        // put your code here
        // int hash = key % size;
        var idx = findKey(key)
        if (idx == -1) {
            rehash()
            idx = findKey(key)
        }
        table[idx] = TableEntry<Any?>(key, value)
        return true
    }

    operator fun get(key: Int): T? {
        // put your code here
        val idx = findKey(key)
        return if (idx == -1 || table[idx] == null) {
            null
        } else table[idx]!!.value as T
    }

    private fun findKey(key: Int): Int {
        // put your code here
        var hash = key % size
        while (!(table[hash] == null || table[hash]!!.key == key)) {
            hash = (hash + 1) % size
            if (hash == key % size) {
                return -1
            }
        }
        return hash
    }

    private fun rehash() {
        size = size * 2
        val tableTemp = table
        table = arrayOfNulls<TableEntry<*>?>(size)
        for (i in tableTemp.indices) {
            if (tableTemp[i] != null) {
                table[findKey(tableTemp[i]!!.key)] = tableTemp[i]
            }
        }
    }

    override fun toString(): String {
        val tableStringBuilder = StringBuilder()
        for (i in table.indices) {
            if (table[i] == null) {
                tableStringBuilder.append("$i: null")
            } else {
                tableStringBuilder.append(i.toString() + ": key=" + table[i]!!.key
                        + ", value=" + table[i]!!.value)
            }
            if (i < table.size - 1) {
                tableStringBuilder.append("\n")
            }
        }
        return tableStringBuilder.toString()
    }

    init {
        table = arrayOfNulls<TableEntry<*>?>(size)
    }
}

fun makeHashTable(fileName: String) {
    var line: String? = null
    var keyh = 7
    try {
        val lines = File(fileName).readLines()
        for (line in lines) {
            val value = line!!.substring(line!!.indexOf(' ') + 1)
            keyh = Math.abs(value.hashCode())
            tab.put(keyh, value)
        }
    } catch (e: Exception) {
        println("File not found.")
    }
}