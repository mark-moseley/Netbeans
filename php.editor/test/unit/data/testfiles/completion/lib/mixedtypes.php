<?php
class Author {
    public $name;
}

/**
 * @property Author $author hello this is doc
 */
class Book {
    /**
     * Title of the book.
     * @var string
     */
    public $title;

    /**
     *
     * @var Author|null
     */
    public $author;

    private function __construct($title) {
        $this->title = $title;
    }

    /**
     *
     * @param string $title
     * @return Book
     */
    public static function createBook($title) {
        return new Book($title);
    }
}

class Magazine {
    public $pages;
}

/**
 * @return Book|Magazine
 */
function getBookMagazine() {

}

getBookMagazine()->pages;

$bm = getBookMagazine();

$bm->author;


/**
 * @return Book|null
 */
function getBook(){

}

getBook()->author;

?>