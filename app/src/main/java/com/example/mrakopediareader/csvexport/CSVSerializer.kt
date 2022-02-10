package com.example.mrakopediareader.csvexport

class CSVSerializer<T>(private val columns: Iterable<CSVColumn<T>>) {
    private fun getHeader(): List<String> {
        return columns.map { it.title }
    }

    private fun getBody(items: Iterable<T>): List<List<String>> {
        val rows = mutableListOf<List<String>>()

        for (item in items) {
            val row = mutableListOf<String>()
            for (column in columns) {
                row.add(column.getContents(item))
            }
            rows.add(row)
        }

        return rows
    }

    private fun getList(items: Iterable<T>): List<List<String>> {
        return mutableListOf<List<String>>().apply {
            add(getHeader())
            addAll(getBody(items))
        }
    }

    private fun csvifyCell(item: String): String {
        val itemWithCSVQuotes = item.replace("\"", "\"\"")
        return "\"$itemWithCSVQuotes\""
    }

    private fun csvifyTable(items: List<List<String>>): List<List<String>> {
        return items.map { row ->
            row.map { cell ->
                csvifyCell(cell)
            }
        }
    }

    private fun serialize(list: List<List<String>>): String {
        return list.joinToString("\n") {
            it.joinToString(",")
        }
    }

    fun serialize(items: Iterable<T>): String {
        val list = getList(items)
        val csvified = csvifyTable(list)
        return serialize(csvified)
    }
}