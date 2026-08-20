namespace BookwormApi.Models;

public enum TransactionStatus
{
    PENDING,
    SUCCESS,
    FAILED
}

public enum TransactionType
{
    BUY,
    RENT,
    LEND
}

public enum LibraryAccessType
{
    RENT,
    LEND
}
