#!/bin/bash
set -e

echo "========================================="
echo "Job Title Search - Solr Schema Setup"
echo "========================================="

SOLR_URL="http://localhost:8983/solr/jobtitles"

echo "⏳ Waiting for Solr to be ready..."
for i in {1..30}; do
  if curl -s "${SOLR_URL}/admin/ping" > /dev/null 2>&1; then
    echo "✅ Solr is ready!"
    break
  fi
  echo "Waiting... ($i/30)"
  sleep 2
done

echo ""
echo "🔧 Adding field types..."

# Add text_en field type
curl -X POST -H "Content-type:application/json" --data-binary '{
  "add-field-type": {
    "name": "text_en",
    "class": "solr.TextField",
    "positionIncrementGap": "100",
    "analyzer": {
      "tokenizer": {"class": "solr.StandardTokenizerFactory"},
      "filters": [
        {"class": "solr.LowerCaseFilterFactory"},
        {"class": "solr.EnglishPossessiveFilterFactory"},
        {"class": "solr.PorterStemFilterFactory"}
      ]
    }
  }
}' "${SOLR_URL}/schema" 2>/dev/null && echo "  ✓ text_en added"

# Add text_fr field type
curl -X POST -H "Content-type:application/json" --data-binary '{
  "add-field-type": {
    "name": "text_fr",
    "class": "solr.TextField",
    "positionIncrementGap": "100",
    "analyzer": {
      "tokenizer": {"class": "solr.StandardTokenizerFactory"},
      "filters": [
        {"class": "solr.LowerCaseFilterFactory"},
        {"class": "solr.ASCIIFoldingFilterFactory"},
        {"class": "solr.FrenchLightStemFilterFactory"}
      ]
    }
  }
}' "${SOLR_URL}/schema" 2>/dev/null && echo "  ✓ text_fr added"

echo ""
echo "📝 Adding fields..."

# Function to add field
add_field() {
  local name=$1
  local type=$2
  curl -X POST -H "Content-type:application/json" --data-binary \
    "{\"add-field\":{\"name\":\"$name\",\"type\":\"$type\",\"stored\":true,\"indexed\":true}}" \
    "${SOLR_URL}/schema" 2>/dev/null && echo "  ✓ $name ($type)"
}

# Add all fields
add_field "noc_code" "string"
add_field "title_en" "text_en"
add_field "title_fr" "text_fr"
add_field "description_en" "text_en"
add_field "description_fr" "text_fr"
add_field "category" "string"
add_field "skill_level" "string"

echo ""
echo "========================================="
echo "✅ Solr schema setup complete!"
echo "========================================="
echo ""
echo "🔗 Solr Admin UI: http://localhost:8983/solr"
echo "🔗 Application: http://localhost:8080"
echo ""