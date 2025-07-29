import re
import sys # Import the sys module to access command-line arguments
import json # Import the json module for parsing JSON data

def reformat_markdown(markdown_text):
  """
  Applies basic reformatting rules to a Markdown text string and
  parses JSON content from code blocks within ordered list items.
  It also attempts to extract an integer property named "code" from parsed JSON
  and inserts it after the list number but before the item text.

  Args:
    markdown_text: A string containing the Markdown content to be reformatted.

  Returns:
    A string containing the reformatted Markdown text.
  """
  lines = markdown_text.splitlines()
  reformatted_lines = []
  in_code_block = False
  code_block_lang = None
  json_content_buffer = [] # Buffer to store lines of a JSON code block

  ordered_list_counter = 0 # Counter for ordered list items
  previous_line_was_ordered_list = False # Flag to track if the previous line was an ordered list item
  # New state variable: True if the current line is logically part of an ordered list item.
  # This helps determine if a nested code block is "within" an ordered list.
  in_ordered_list_item_context = False
  # Stores the index in reformatted_lines of the last ordered list item processed.
  # This allows us to go back and modify that line if a nested JSON 'code' is found.
  last_ordered_list_item_idx = -1

  for i, line in enumerate(lines):
    stripped_line = line.strip()

    # --- Code Block Handling (Rule 3 and JSON parsing) ---
    if stripped_line.startswith("```"):
      if not in_code_block:
        # Entering a code block
        # Extract language if present (e.g., ```json or ``` json)
        match_lang = re.match(r"```\s*(\S*)", stripped_line)
        if match_lang:
          code_block_lang = match_lang.group(1).lower()
        else:
          code_block_lang = None # No language specified

        # # Add blank line before if previous line isn't blank
        # if i > 0 and lines[i-1].strip() != "":
        #   reformatted_lines.append("")
        in_code_block = True
        reformatted_lines.append(line) # Append the ``` line
      else:
        # Exiting a code block
        reformatted_lines.append(line) # Append the closing ``` line

        # Only attempt JSON parsing if we were in an ordered list item context
        # when the code block started AND it's a JSON block
        if in_ordered_list_item_context and code_block_lang == "json":
          json_text = "\n".join(json_content_buffer)
          try:
            parsed_json = json.loads(json_text)
            print(f"\n--- Found and Parsed JSON in Code Block WITHIN Ordered List Item (Lines {i - len(json_content_buffer)} to {i-1}) ---")
            print(f"Parsed JSON: {parsed_json}")

            # Attempt to get the 'code' property as an integer
            if isinstance(parsed_json, dict) and "code" in parsed_json:
              code_value = parsed_json["code"]
              if isinstance(code_value, int):
                print(f"Extracted 'code' property (integer): {code_value}")
                # Prepend the code value to the last processed ordered list item
                # This assumes the JSON block is directly nested under the list item.
                if last_ordered_list_item_idx != -1 and last_ordered_list_item_idx < len(reformatted_lines):
                    original_list_line = reformatted_lines[last_ordered_list_item_idx]
                    # Re-match to extract parts for precise insertion
                    list_line_match = re.match(r"^(\s*)(\d+)\.\s*(.*)", original_list_line)
                    if list_line_match:
                        indentation = list_line_match.group(1)
                        number = list_line_match.group(2)
                        item_text = list_line_match.group(3)
                        id_frag = "#id"
                        test_case_link = f"Test case [{code_value}]({id_frag}-testCase{code_value}){{ {id_frag}-testCase{code_value} }}:"
                        # NEW: Insert code_value after the number and period
                        reformatted_lines[last_ordered_list_item_idx] = f"{indentation}{number}. {test_case_link} {item_text}"
                        print(f"Modified list item at index {last_ordered_list_item_idx}: {reformatted_lines[last_ordered_list_item_idx]}")
                    else:
                        print(f"Warning: Last ordered list item at index {last_ordered_list_item_idx} no longer appears to be an ordered list item. Skipping prepend.")
                else:
                    print("Warning: JSON 'code' found but no valid last ordered list item index to attach it to.")
              else:
                print(f"Found 'code' property, but it is not an integer: {code_value} (type: {type(code_value).__name__})")
            else:
              print("No 'code' property found in the parsed JSON or it's not a dictionary.")

          except json.JSONDecodeError as e:
            print(f"\n--- Error Parsing JSON in Code Block WITHIN Ordered List Item (Lines {i - len(json_content_buffer)} to {i-1}) ---")
            print(f"JSON Decode Error: {e}")
            print(f"Problematic JSON content:\n{json_text}")
          finally:
            json_content_buffer = [] # Clear buffer after attempt to parse

        # # Add blank line after if next line isn't blank
        # if i < len(lines) - 1 and lines[i+1].strip() != "":
        #   reformatted_lines.append("")

        in_code_block = False
        code_block_lang = None
        # Reset list-related state after a code block, as it typically breaks the list flow
        ordered_list_counter = 0
        previous_line_was_ordered_list = False
        in_ordered_list_item_context = False
        last_ordered_list_item_idx = -1 # Reset this as well

      continue # Skip other rules for ``` lines

    elif in_code_block:
      # If inside a code block, just append the line to the buffer if it's JSON
      if code_block_lang == "json":
        json_content_buffer.append(line)
      reformatted_lines.append(line)
      continue # Skip other rules for lines within code blocks

    # --- Apply other reformatting rules if not in a code block ---

    # # Rule 1: Ensure consistent spacing after header hashes
    # if re.match(r"^(#+)(\S.*)", line):
    #   line = re.sub(r"^(#+)(\S.*)", r"\1 \2", line)

    # # Rule 2: Standardize unordered list markers to '*'
    # # Convert '-' or '+' to '*' at the start of a list item
    # if re.match(r"^(\s*)[-+](?=\s+)", line):
    #   line = re.sub(r"^(\s*)[-+](?=\s+)", r"\1* ", line)

    # Rule 4: Reformat ordered lists to ensure sequential numbering
    # Detect lines that look like ordered list items (e.g., "1. Item", "5. Another item")
    ordered_list_match = re.match(r"^(\s*)(\d+)\.\s*(.*)", line)
    if ordered_list_match:
      # If this is the first item of a new list or a continuation, increment counter
      if not previous_line_was_ordered_list:
        ordered_list_counter = 1
      else:
        ordered_list_counter += 1

      indentation = ordered_list_match.group(1)
      order_number = ordered_list_match.group(2)
      item_text = ordered_list_match.group(3)
      # Construct the reformatted list item line
      # line_to_add = f"{indentation}{ordered_list_counter}. {item_text}"
      line_to_add = f"{indentation}{order_number}. {item_text}"
      reformatted_lines.append(line_to_add) # Add it to the list
      # Store the index of this newly added ordered list item
      last_ordered_list_item_idx = len(reformatted_lines) - 1

      previous_line_was_ordered_list = True
      in_ordered_list_item_context = True # Set context when an ordered list item is found
    elif len(line.strip()) == 0:
      # If the current line is whitespace, reset the counter and context
      ordered_list_counter = 0
      previous_line_was_ordered_list = False
      in_ordered_list_item_context = False
      last_ordered_list_item_idx = -1 # Reset this if not an OL item

      reformatted_lines.append(line) # Append non-list lines directly
    else:
      reformatted_lines.append(line) # Append non-list lines directly

  # Join the lines back into a single string
  return "\n".join(reformatted_lines)

# --- Example Usage ---
if __name__ == "__main__":
  # Check if a file path is provided as a command-line argument
  if len(sys.argv) < 2:
    print("Usage: python your_script_name.py <markdown_file_path>")
    print("Example: python your_script_name.py my_document.md")
    sys.exit(1) # Exit with an error code

  file_path = sys.argv[1] # Get the file path from the first argument

  try:
    # Read the content of the specified Markdown file
    with open(file_path, 'r', encoding='utf-8') as f:
      markdown_content = f.read()

    print(f"--- Original Markdown from '{file_path}' ---")
    print(markdown_content)
    print("\n" + "="*30 + "\n")

    # Reformat the Markdown text
    reformatted_result = reformat_markdown(markdown_content)

    if len(sys.argv) > 2:
      output_file_path = sys.argv[2]
      if output_file_path:
        print(f"writing to file {output_file_path}")
        with open(output_file_path, "w", encoding='utf-8') as f:
          f.write(reformatted_result)
        print(f"\nReformatted Markdown saved to '{output_file_path}'")
    else:
      print("--- Reformatted Markdown ---")
      print(reformatted_result)
    # Optional: Save the reformatted content to a new file (e.g., 'reformatted_my_document.md')
    # You might want to prompt the user for an output file name or use a default.
    # output_file_path = f"reformatted_{file_path}"
    # with open(output_file_path, "w", encoding='utf-8') as f:
    #   f.write(reformatted_result)
    # print(f"\nReformatted Markdown saved to '{output_file_path}'")

  except FileNotFoundError:
    print(f"Error: The file '{file_path}' was not found.")
    sys.exit(1)
  except Exception as e:
    print(f"An error occurred: {e}")
    sys.exit(1)

