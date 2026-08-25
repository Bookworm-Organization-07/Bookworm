namespace BookwormApi.Models;

public class Author
{
    public int AuthorId { get; set; }
    public string Name { get; set; } = null!;
    public string? Bio { get; set; }
}

public class Publisher
{
    public int PublisherId { get; set; }
    public string Name { get; set; } = null!;
    public string Email { get; set; } = null!;
}

public class Language
{
    public int LanguageId { get; set; }
    public string LanguageDesc { get; set; } = null!;
}

public class Genere
{
    public int GenereId { get; set; }
    public string GenereDesc { get; set; } = null!;
}

public class ProductType
{
    public int TypeId { get; set; }
    public string TypeDesc { get; set; } = null!;
}

public class AttributeEntity
{
    public int AttributeId { get; set; }
    public string AttributeDesc { get; set; } = null!;
}
