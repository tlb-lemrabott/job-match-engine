from flask import Flask, request, jsonify
from flask_cors import CORS
import numpy as np
from sentence_transformers import SentenceTransformer
import spacy
import re
from collections import Counter
import logging
from typing import List, Dict, Set, Tuple
import json

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)

class ResumeJobMatcher:
    def __init__(self):
        """Initialize the matcher with pre-trained models"""
        logger.info("Loading AI models...")
        
        # Load sentence transformer for semantic similarity
        self.sentence_model = SentenceTransformer('all-MiniLM-L6-v2')
        
        # Load spaCy for NLP tasks
        try:
            self.nlp = spacy.load("en_core_web_sm")
        except OSError:
            logger.warning("spaCy model not found. Installing...")
            import subprocess
            subprocess.run(["python", "-m", "spacy", "download", "en_core_web_sm"])
            self.nlp = spacy.load("en_core_web_sm")
        
        # Define skill patterns and variations
        self.skill_patterns = {
            'programming_languages': [
                r'\b(python|java|javascript|typescript|c\+\+|c#|go|rust|swift|kotlin|scala|php|ruby|r|matlab|perl|bash|shell)\b',
                r'\b(html|css|sql|nosql|mongodb|postgresql|mysql|oracle|sqlite)\b'
            ],
            'frameworks_libraries': [
                r'\b(spring|spring boot|django|flask|fastapi|express|react|angular|vue|node\.js|jquery|bootstrap|tailwind)\b',
                r'\b(tensorflow|pytorch|scikit-learn|pandas|numpy|matplotlib|seaborn|plotly)\b',
                r'\b(docker|kubernetes|jenkins|git|github|gitlab|bitbucket|jira|confluence)\b'
            ],
            'cloud_platforms': [
                r'\b(aws|amazon web services|azure|google cloud|gcp|heroku|digitalocean|linode|vultr)\b',
                r'\b(ec2|s3|lambda|rds|dynamodb|cloudfront|route53|vpc|iam|sagemaker)\b'
            ],
            'databases': [
                r'\b(mysql|postgresql|mongodb|redis|cassandra|elasticsearch|dynamodb|firebase|supabase)\b'
            ],
            'methodologies': [
                r'\b(agile|scrum|kanban|waterfall|devops|ci/cd|tdd|bdd|lean|six sigma)\b'
            ],
            'soft_skills': [
                r'\b(leadership|communication|teamwork|problem solving|critical thinking|time management)\b',
                r'\b(project management|collaboration|mentoring|presentation|negotiation|adaptability)\b'
            ]
        }
        
        # Experience patterns
        self.experience_patterns = [
            r'(\d+)\+?\s*years?\s*(?:of\s*)?(?:experience\s*)?(?:in\s*)?([^,\.]+)',
            r'(\d+)\+?\s*years?\s*([^,\.]+)',
            r'([^,\.]+)\s*(\d+)\+?\s*years?'
        ]
        
        logger.info("AI models loaded successfully!")

    def extract_skills(self, text: str) -> List[str]:
        """Extract skills from text using multiple approaches"""
        skills = set()
        text_lower = text.lower()
        
        # Extract using regex patterns
        for category, patterns in self.skill_patterns.items():
            for pattern in patterns:
                matches = re.findall(pattern, text_lower)
                skills.update(matches)
        
        # Extract using spaCy NER and noun chunks
        doc = self.nlp(text)
        
        # Extract named entities that might be skills
        for ent in doc.ents:
            if ent.label_ in ['ORG', 'PRODUCT', 'GPE']:
                skills.add(ent.text.lower())
        
        # Extract noun chunks that might be skills
        for chunk in doc.noun_chunks:
            if len(chunk.text.split()) <= 3:  # Skills are usually 1-3 words
                skills.add(chunk.text.lower())
        
        # Clean and filter skills
        cleaned_skills = []
        for skill in skills:
            skill = skill.strip()
            if len(skill) >= 2 and len(skill) <= 50:  # Reasonable skill length
                cleaned_skills.append(skill)
        
        return list(set(cleaned_skills))

    def extract_experience(self, text: str) -> List[Dict[str, str]]:
        """Extract experience requirements from text"""
        experience_requirements = []
        text_lower = text.lower()
        
        for pattern in self.experience_patterns:
            matches = re.findall(pattern, text_lower)
            for match in matches:
                if len(match) == 2:
                    years, skill = match
                    experience_requirements.append({
                        'years': years.strip(),
                        'skill': skill.strip()
                    })
        
        return experience_requirements

    def calculate_semantic_similarity(self, text1: str, text2: str) -> float:
        """Calculate semantic similarity between two texts"""
        try:
            # Encode texts to vectors
            embeddings = self.sentence_model.encode([text1, text2])
            
            # Calculate cosine similarity
            similarity = np.dot(embeddings[0], embeddings[1]) / (
                np.linalg.norm(embeddings[0]) * np.linalg.norm(embeddings[1])
            )
            
            return float(similarity)
        except Exception as e:
            logger.error(f"Error calculating semantic similarity: {e}")
            return 0.0

    def find_skill_matches(self, resume_skills: List[str], job_skills: List[str]) -> List[str]:
        """Find matching skills between resume and job description"""
        matched_skills = []
        resume_skills_set = set(skill.lower() for skill in resume_skills)
        job_skills_set = set(skill.lower() for skill in job_skills)
        
        # Direct matches
        direct_matches = resume_skills_set.intersection(job_skills_set)
        matched_skills.extend(list(direct_matches))
        
        # Fuzzy matches using skill variations
        skill_variations = {
            'java': ['j2ee', 'jee', 'spring', 'hibernate', 'maven', 'gradle'],
            'javascript': ['js', 'es6', 'node', 'react', 'angular', 'vue', 'typescript'],
            'python': ['django', 'flask', 'fastapi', 'pandas', 'numpy', 'scikit-learn'],
            'aws': ['amazon web services', 'ec2', 's3', 'lambda', 'rds', 'dynamodb'],
            'docker': ['containerization', 'kubernetes', 'k8s', 'containers'],
            'sql': ['mysql', 'postgresql', 'oracle', 'sql server', 'database'],
            'git': ['github', 'gitlab', 'bitbucket', 'version control'],
            'agile': ['scrum', 'kanban', 'sprint', 'backlog', 'user stories']
        }
        
        for skill in resume_skills_set:
            for base_skill, variations in skill_variations.items():
                if skill in variations or base_skill in skill:
                    for job_skill in job_skills_set:
                        if job_skill in variations or base_skill in job_skill:
                            if skill not in matched_skills:
                                matched_skills.append(skill)
        
        return list(set(matched_skills))

    def find_missing_skills(self, resume_skills: List[str], job_skills: List[str]) -> List[str]:
        """Find skills required by job but missing from resume"""
        matched_skills = set(self.find_skill_matches(resume_skills, job_skills))
        job_skills_set = set(skill.lower() for skill in job_skills)
        
        missing_skills = []
        for job_skill in job_skills_set:
            if job_skill not in matched_skills:
                missing_skills.append(job_skill)
        
        return missing_skills

    def find_missing_experience(self, resume_text: str, job_experience: List[Dict[str, str]]) -> List[str]:
        """Find missing experience requirements"""
        missing_experience = []
        resume_experience = self.extract_experience(resume_text)
        
        for job_req in job_experience:
            job_skill = job_req['skill']
            job_years = int(job_req['years'])
            
            # Check if resume has this skill with enough experience
            found = False
            for resume_exp in resume_experience:
                if job_skill in resume_exp['skill']:
                    resume_years = int(resume_exp['years'])
                    if resume_years >= job_years:
                        found = True
                        break
            
            if not found:
                missing_experience.append(f"{job_years}+ years in {job_skill}")
        
        return missing_experience

    def calculate_match_score(self, semantic_similarity: float, matched_skills: List[str], 
                            missing_skills: List[str], job_skills: List[str]) -> float:
        """Calculate overall match score"""
        if not job_skills:
            return semantic_similarity * 100
        
        # Skill matching score
        skill_match_ratio = len(matched_skills) / len(job_skills)
        
        # Missing skills penalty
        missing_penalty = len(missing_skills) / len(job_skills) if job_skills else 0
        
        # Weighted combination
        skill_score = (skill_match_ratio * 0.6) - (missing_penalty * 0.4)
        semantic_score = semantic_similarity * 0.4
        
        final_score = (semantic_score + skill_score) * 100
        
        return max(0.0, min(100.0, final_score))

    def analyze_job_match(self, resume_text: str, job_description: str) -> Dict:
        """Main method to analyze job match"""
        try:
            logger.info("Starting job match analysis...")
            
            # Extract skills
            resume_skills = self.extract_skills(resume_text)
            job_skills = self.extract_skills(job_description)
            
            # Extract experience requirements
            job_experience = self.extract_experience(job_description)
            
            # Calculate semantic similarity
            semantic_similarity = self.calculate_semantic_similarity(resume_text, job_description)
            
            # Find matches and gaps
            matched_skills = self.find_skill_matches(resume_skills, job_skills)
            missing_skills = self.find_missing_skills(resume_skills, job_skills)
            missing_experience = self.find_missing_experience(resume_text, job_experience)
            
            # Calculate overall score
            match_score = self.calculate_match_score(
                semantic_similarity, matched_skills, missing_skills, job_skills
            )
            
            # Prepare response
            result = {
                "matchScore": round(match_score, 2),
                "matchedSkills": matched_skills,
                "missingSkills": missing_skills,
                "missingExperience": missing_experience,
                "otherMissing": []  # Placeholder for additional missing requirements
            }
            
            logger.info(f"Analysis completed. Match score: {match_score}")
            return result
            
        except Exception as e:
            logger.error(f"Error in job match analysis: {e}")
            return {
                "matchScore": 0.0,
                "matchedSkills": [],
                "missingSkills": [],
                "missingExperience": [],
                "otherMissing": [f"Analysis error: {str(e)}"]
            }

# Initialize the matcher
matcher = ResumeJobMatcher()

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({"status": "healthy", "service": "python-job-matcher"})

@app.route('/analyze', methods=['POST'])
def analyze_job_match():
    """Main endpoint for job matching analysis"""
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({"error": "No data provided"}), 400
        
        resume_text = data.get('resume_text', '')
        job_description = data.get('job_description', '')
        
        if not resume_text or not job_description:
            return jsonify({"error": "Both resume_text and job_description are required"}), 400
        
        logger.info(f"Received analysis request. Resume length: {len(resume_text)}, Job description length: {len(job_description)}")
        
        # Perform analysis
        result = matcher.analyze_job_match(resume_text, job_description)
        
        return jsonify(result)
        
    except Exception as e:
        logger.error(f"Error processing request: {e}")
        return jsonify({"error": f"Internal server error: {str(e)}"}), 500

@app.route('/skills', methods=['POST'])
def extract_skills():
    """Endpoint to extract skills from text"""
    try:
        data = request.get_json()
        text = data.get('text', '')
        
        if not text:
            return jsonify({"error": "Text is required"}), 400
        
        skills = matcher.extract_skills(text)
        return jsonify({"skills": skills})
        
    except Exception as e:
        logger.error(f"Error extracting skills: {e}")
        return jsonify({"error": f"Internal server error: {str(e)}"}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001, debug=True)
